package com.mogou.model;

public enum StageState {
    CANDIDATURE_SOUMISE,
    CANDIDATURE_ACCEPTEE,
    CONVENTION_GENEREE,
    CONVENTION_VALIDEE_ENSEIGNANT,
    CONVENTION_SIGNEE,
    STAGE_APPROUVE_ADMIN,
    STAGE_TERMINE,
    ERREUR // Un état pour gérer les échecs
}
