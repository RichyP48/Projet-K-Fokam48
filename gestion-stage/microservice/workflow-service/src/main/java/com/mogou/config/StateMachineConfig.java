package com.mogou.config;


import com.mogou.model.StageEvent;
import com.mogou.model.StageState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<StageState, StageEvent> {

    // Injection de nos beans d'actions et de gardes
    private final Guard<StageState, StageEvent> checkCandidatureAcceptedGuard;
    private final Action<StageState, StageEvent> genererConventionAction;

    @Override
    public void configure(StateMachineStateConfigurer<StageState, StageEvent> states) throws Exception {
        states
                .withStates()
                .initial(StageState.CANDIDATURE_SOUMISE)
                .states(EnumSet.allOf(StageState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<StageState, StageEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(StageState.CANDIDATURE_SOUMISE).target(StageState.CANDIDATURE_ACCEPTEE)
                .event(StageEvent.ACCEPTER_CANDIDATURE)
                .and()
                .withExternal()
                .source(StageState.CANDIDATURE_ACCEPTEE).target(StageState.CONVENTION_GENEREE)
                .event(StageEvent.GENERER_CONVENTION)
                .guard(checkCandidatureAcceptedGuard) // <-- NOTRE GARDE EST ATTACHÉE ICI
                .action(genererConventionAction);    // <-- NOTRE ACTION EST ATTACHÉE ICI
        // ... configurer les autres transitions avec leurs gardes et actions
    }
}
