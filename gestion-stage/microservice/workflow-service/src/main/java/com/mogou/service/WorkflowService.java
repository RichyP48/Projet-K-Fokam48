package com.mogou.service;

import com.mogou.model.StageEvent;
import com.mogou.model.StageState;
import com.mogou.model.StageWorkflowInstance;
import com.mogou.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final StateMachineFactory<StageState, StageEvent> stateMachineFactory;
    private final WorkflowInstanceRepository workflowRepository; // <-- L'INJECTION EST MAINTENANT VALIDE

    public StageState getCurrentState(Long candidatureId) {
        // CET APPEL EST MAINTENANT VALIDE
        return workflowRepository.findById(candidatureId)
                .map(StageWorkflowInstance::getCurrentState)
                .orElse(StageState.CANDIDATURE_SOUMISE);
    }

    @Transactional
    public boolean sendEvent(Long candidatureId, StageEvent event) {
        StateMachine<StageState, StageEvent> sm = this.build(candidatureId);

        boolean success = sm.sendEvent(event);

        if (success) {
            // CET APPEL EST MAINTENANT VALIDE
            StageWorkflowInstance instance = workflowRepository.findById(candidatureId).orElse(new StageWorkflowInstance());
            instance.setCandidatureId(candidatureId);
            instance.setCurrentState(sm.getState().getId());
            workflowRepository.save(instance); // <-- CET APPEL EST MAINTENANT VALIDE
        }

        return success;
    }

    private StateMachine<StageState, StageEvent> build(Long candidatureId) {
        // CET APPEL EST MAINTENANT VALIDE
        StageWorkflowInstance instance = workflowRepository.findById(candidatureId)
                .orElseGet(() -> {
                    StageWorkflowInstance newInstance = new StageWorkflowInstance();
                    newInstance.setCandidatureId(candidatureId);
                    newInstance.setCurrentState(StageState.CANDIDATURE_SOUMISE);
                    return newInstance;
                });

        StateMachine<StageState, StageEvent> sm = stateMachineFactory.getStateMachine(Long.toString(instance.getCandidatureId()));
        sm.stop();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.resetStateMachine(new DefaultStateMachineContext<>(instance.getCurrentState(), null, null, null));
                });
        sm.start();
        return sm;
    }
}
