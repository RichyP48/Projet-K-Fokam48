package com.mogou.statemachine;

import com.mogou.enums.StatutCandidature;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<StatutCandidature, CandidatureEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<StatutCandidature, CandidatureEvent> states) throws Exception {
        states
                .withStates()
                .initial(StatutCandidature.POSTULE)
                .states(EnumSet.allOf(StatutCandidature.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<StatutCandidature, CandidatureEvent> transitions) throws Exception {
        transitions
                .withExternal().source(StatutCandidature.POSTULE).target(StatutCandidature.EN_ATTENTE).event(CandidatureEvent.EXAMINER)
                .and()
                .withExternal().source(StatutCandidature.EN_ATTENTE).target(StatutCandidature.ACCEPTE).event(CandidatureEvent.ACCEPTER)
                .and()
                .withExternal().source(StatutCandidature.EN_ATTENTE).target(StatutCandidature.REFUSE).event(CandidatureEvent.REFUSER)
                .and()
                .withExternal().source(StatutCandidature.POSTULE).target(StatutCandidature.RETIRE).event(CandidatureEvent.RETIRER)
                .and()
                .withExternal().source(StatutCandidature.EN_ATTENTE).target(StatutCandidature.RETIRE).event(CandidatureEvent.RETIRER);
    }
}
