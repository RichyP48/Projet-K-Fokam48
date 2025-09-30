package com.mogou.service;

import com.mogou.client.ApplicationClient;
import com.mogou.dto.CandidatureValidationDto;
import com.mogou.dto.MessageDto;
import com.mogou.dto.MessageMapper;
import com.mogou.exception.UnauthorizedConversationAccessException;
import com.mogou.model.Conversation;
import com.mogou.model.Message;
import com.mogou.repository.ConversationRepository;
import com.mogou.repository.MessageRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final ApplicationClient applicationClient; // <-- INJECTION DU CLIENT FEIGN

    @Override
    @Transactional
    public MessageDto sendMessage(Long candidatureId, MessageDto messageDto, String username) {
        // TODO: Extraire le 'senderId' et le 'senderType' de l'utilisateur authentifié (via le token JWT)
        Long currentUserId = 1L; // Exemple

        // 1. VÉRIFICATION DES DROITS
        CandidatureValidationDto validationDto = checkUserPermission(candidatureId, currentUserId);

        // 2. Trouver ou créer la conversation
        Conversation conversation = conversationRepository.findByCandidatureId(candidatureId)
                .orElseGet(() -> createConversation(candidatureId, validationDto.getEtudiantId(), validationDto.getEntrepriseId()));

        // 3. Créer et sauvegarder le message
        Message message = messageMapper.toEntity(messageDto);
        message.setConversationId(conversation.getId());
        message.setSentAt(LocalDateTime.now());
        message.setSenderId(messageDto.getSenderId());
        message.setSenderType(messageDto.getSenderType());

        Message savedMessage = messageRepository.save(message);

        // 4. Mettre à jour la conversation
        conversation.setLastMessageAt(savedMessage.getSentAt());
        conversationRepository.save(conversation);

        return messageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> getConversationHistory(Long candidatureId) {
        // TODO: Extraire le 'senderId' de l'utilisateur authentifié
        Long currentUserId = 1L; // Exemple

        // VÉRIFICATION DES DROITS
        checkUserPermission(candidatureId, currentUserId);

        Conversation conversation = conversationRepository.findByCandidatureId(candidatureId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId())
                .stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Méthode privée pour valider si un utilisateur a le droit d'accéder à une conversation.
     * Appelle le 'applications-service' pour obtenir les détails.
     */
    private CandidatureValidationDto checkUserPermission(Long candidatureId, Long userId) {
        try {
            CandidatureValidationDto validationDto = applicationClient.getCandidatureForValidation(candidatureId);

            boolean isParticipant = userId.equals(validationDto.getEtudiantId()) || userId.equals(validationDto.getEntrepriseId());

            if (!isParticipant) {
                throw new UnauthorizedConversationAccessException("L'utilisateur " + userId + " ne fait pas partie de la candidature " + candidatureId);
            }

            // Optionnel: N'autoriser le chat que si la candidature est acceptée
            if (!validationDto.isEstAcceptee()) {
                throw new UnauthorizedConversationAccessException("La messagerie est uniquement disponible pour les candidatures acceptées.");
            }

            return validationDto;
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Candidature non trouvée avec l'ID : " + candidatureId);
        }
    }

    private Conversation createConversation(Long candidatureId, Long etudiantId, Long entrepriseId) {
        Conversation newConversation = new Conversation();
        newConversation.setCandidatureId(candidatureId);
        newConversation.setEtudiantId(etudiantId);
        newConversation.setEntrepriseId(entrepriseId);
        return conversationRepository.save(newConversation);
    }
}

