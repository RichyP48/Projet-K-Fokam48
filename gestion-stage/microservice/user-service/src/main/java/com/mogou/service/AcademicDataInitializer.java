package com.mogou.service;

import com.mogou.model.School;
import com.mogou.model.Faculty;
import com.mogou.model.Department;
import com.mogou.repository.SchoolRepository;
import com.mogou.repository.FacultyRepository;
import com.mogou.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcademicDataInitializer implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) {
        if (schoolRepository.count() == 0) {
            initializeAcademicData();
        }
    }

    private void initializeAcademicData() {
        // Écoles
        School uy1 = createSchool("Université de Yaoundé I", "Yaoundé, Cameroun");
        School ud = createSchool("Université de Douala", "Douala, Cameroun");
        School ensp = createSchool("ENSP Yaoundé", "Yaoundé, Cameroun");

        // Facultés UY1
        Faculty sciencesUy1 = createFaculty("Faculté des Sciences", uy1);
        Faculty medecineUy1 = createFaculty("Faculté de Médecine", uy1);
        Faculty ensUy1 = createFaculty("École Normale Supérieure", uy1);

        // Facultés UD
        Faculty ecoUd = createFaculty("Faculté des Sciences Économiques", ud);
        Faculty lettresUd = createFaculty("Faculté des Lettres", ud);

        // Facultés ENSP
        Faculty genieInfo = createFaculty("Génie Informatique", ensp);
        Faculty genieCivil = createFaculty("Génie Civil", ensp);

        // Départements
        createDepartment("Informatique", sciencesUy1);
        createDepartment("Mathématiques", sciencesUy1);
        createDepartment("Physique", sciencesUy1);
        createDepartment("Médecine Générale", medecineUy1);
        createDepartment("Chirurgie", medecineUy1);
        createDepartment("Enseignement Primaire", ensUy1);
        createDepartment("Économie", ecoUd);
        createDepartment("Gestion", ecoUd);
        createDepartment("Français", lettresUd);
        createDepartment("Anglais", lettresUd);
        createDepartment("Génie Logiciel", genieInfo);
        createDepartment("Réseaux et Télécommunications", genieInfo);
        createDepartment("Bâtiment et Travaux Publics", genieCivil);
    }

    private School createSchool(String name, String address) {
        School school = new School();
        school.setName(name);
        school.setAddress(address);
        return schoolRepository.save(school);
    }

    private Faculty createFaculty(String name, School school) {
        Faculty faculty = new Faculty();
        faculty.setName(name);
        faculty.setSchool(school);
        return facultyRepository.save(faculty);
    }

    private Department createDepartment(String name, Faculty faculty) {
        Department department = new Department();
        department.setName(name);
        department.setFaculty(faculty);
        return departmentRepository.save(department);
    }
}