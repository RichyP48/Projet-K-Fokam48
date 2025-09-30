import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule, FormArray } from '@angular/forms';
import { SchoolService, School, Faculty } from '../../../services/school.service';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, FormsModule],
  template: `
    <div class="flex">
      <div class="h-screen flex flex-col items-center justify-center bg-gradient-to-r from-[#2dd4bf] to-[#1f2937] w-full overflow-y-auto">
        <div class="w-full max-w-md p-8 space-y-6 bg-transparent rounded-lg shadow-md my-8"  style="backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); background-color: rgba(255, 255, 255, 0.1);">
          <h2 class="text-2xl font-bold text-center text-gray-900">
            Inscription
          </h2>

          <!-- Role Selection -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">Type de compte</label>
            <div class="grid grid-cols-3 gap-2">
              <button 
                type="button"
                (click)="selectRole('STUDENT')"
                [class]="selectedRole === 'STUDENT' ? 'bg-primary-600 text-white' : 'bg-white text-primary-600'"
                class="px-3 py-2 border border-primary-600 rounded-md text-sm font-medium hover:bg-primary-50">
                Étudiant
              </button>
              <button 
                type="button"
                (click)="selectRole('COMPANY')"
                [class]="selectedRole === 'COMPANY' ? 'bg-primary-600 text-white' : 'bg-white text-primary-600'"
                class="px-3 py-2 border border-primary-600 rounded-md text-sm font-medium hover:bg-primary-50">
                Entreprise
              </button>
              <button 
                type="button"
                (click)="selectRole('FACULTY')"
                [class]="selectedRole === 'FACULTY' ? 'bg-primary-600 text-white' : 'bg-white text-primary-600'"
                class="px-3 py-2 border border-primary-600 rounded-md text-sm font-medium hover:bg-primary-50">
                Établissement
              </button>
            </div>
          </div>

          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()" class="space-y-4">
            
            <!-- Student Fields -->
            <div *ngIf="selectedRole === 'STUDENT'">
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">Prénom</label>
                  <input
                    formControlName="firstName"
                    type="text"
                    class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    [ngClass]="{ 'border-red-500': registerForm.get('firstName')?.invalid && registerForm.get('firstName')?.touched }"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">Nom</label>
                  <input
                    formControlName="lastName"
                    type="text"
                    class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    [ngClass]="{ 'border-red-500': registerForm.get('lastName')?.invalid && registerForm.get('lastName')?.touched }"
                  />
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Email</label>
                <input
                  formControlName="email"
                  type="email"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('email')?.invalid && registerForm.get('email')?.touched }"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Téléphone</label>
                <input
                  formControlName="phoneNumber"
                  type="tel"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">École</label>
                <select
                  formControlName="schoolId"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('schoolId')?.invalid && registerForm.get('schoolId')?.touched }"
                  (change)="onSchoolChange($event)">
                  <option value="">Sélectionnez une école</option>
                  <option *ngFor="let school of schools" [value]="school.id">{{school.name}}</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Faculté</label>
                <select
                  formControlName="facultyId"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('facultyId')?.invalid && registerForm.get('facultyId')?.touched }"
                  [disabled]="!selectedSchoolId"
                  (change)="onFacultyChange($event)">
                  <option value="">Sélectionnez une faculté</option>
                  <option *ngFor="let faculty of faculties" [value]="faculty.id">{{faculty.name}}</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Département</label>
                <select
                  formControlName="departmentId"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('departmentId')?.invalid && registerForm.get('departmentId')?.touched }"
                  [disabled]="!selectedFacultyId">
                  <option value="">Sélectionnez un département</option>
                  <option *ngFor="let department of departments" [value]="department.id">{{department.name}}</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Niveau d'études</label>
                <select
                  formControlName="studyLevel"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm">
                  <option value="">Sélectionnez votre niveau</option>
                  <option value="L1">Licence 1</option>
                  <option value="L2">Licence 2</option>
                  <option value="L3">Licence 3</option>
                  <option value="M1">Master 1</option>
                  <option value="M2">Master 2</option>
                  <option value="D">Doctorat</option>
                </select>
              </div>
            </div>

            <!-- Faculty/School Fields -->
            <div *ngIf="selectedRole === 'FACULTY'" class="">
              <div>
              <div>
                <label class="block text-sm font-medium text-gray-700">Email de contact</label>
                <input
                  formControlName="contactEmail"
                  type="email"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('contactEmail')?.invalid && registerForm.get('contactEmail')?.touched }"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Nom de l'établissement</label>
                <input
                  formControlName="schoolName"
                  type="text"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('schoolName')?.invalid && registerForm.get('schoolName')?.touched }"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Adresse</label>
                <input
                  formControlName="schoolAddress"
                  type="text"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Description</label>
                <textarea
                  formControlName="schoolDescription"
                  rows="2"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                ></textarea>
              </div></div>
              
              <!-- Facultés dynamiques -->
               <div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Facultés et Départements</label>
                <div formArrayName="faculties">
                  <div *ngFor="let faculty of getFacultiesArray().controls; let i = index" [formGroupName]="i" class="border p-4 mb-4 rounded-md bg-gray-50">
                    <div class="flex justify-between items-center mb-2">
                      <h4 class="font-medium">Faculté {{i + 1}}</h4>
                      <button type="button" (click)="removeFaculty(i)" class="text-red-600 hover:text-red-800">Supprimer</button>
                    </div>
                    
                    <div class="grid grid-cols-2 gap-4 mb-3">
                      <select formControlName="name" class="px-3 py-2 border rounded-md">
                        <option value="">Sélectionnez une faculté</option>
                        <option *ngFor="let name of facultyNames" [value]="name">{{name}}</option>
                      </select>
                      <input formControlName="description" placeholder="Description (optionnel)" class="px-3 py-2 border rounded-md" />
                    </div>
                    
                    <!-- Départements -->
                    <div formArrayName="departments" >
                      <label class="block text-sm font-medium text-gray-600 mb-2">Départements</label>
                      <div class="flex flex-wrap" *ngFor="let dept of getDepartmentsArray(i).controls; let j = index" [formGroupName]="j" class="grid grid-cols-4 gap-2 mb-2">
                        <select formControlName="name" class="px-2 py-1 border rounded text-sm">
                          <option value="">Département</option>
                          <option *ngFor="let name of departmentNames" [value]="name">{{name}}</option>
                        </select>
                        <input formControlName="headTeacherFirstName" placeholder="Prénom enseignant" class="px-2 py-1 border rounded text-sm" />
                        <input formControlName="headTeacherLastName" placeholder="Nom enseignant" class="px-2 py-1 border rounded text-sm" />
                        <div class="flex">
                          <input formControlName="headTeacherEmail" placeholder="Email enseignant" class="px-2 py-1 border rounded text-sm flex-1" />
                          <button type="button" (click)="removeDepartment(i, j)" class="ml-1 text-red-600 text-sm">×</button>
                        </div>
                      </div>
                      <button type="button" (click)="addDepartment(i)" class="text-blue-600 text-sm hover:underline">+ Ajouter département</button>
                    </div>
                  </div>
                </div>
                <button type="button" (click)="addFaculty()" class="text-blue-600 hover:underline">+ Ajouter faculté</button>
              </div>
            </div>
            </div>

            <!-- Company Fields -->
            <div *ngIf="selectedRole === 'COMPANY'">
              <div>
                <label class="block text-sm font-medium text-gray-700">Email de contact</label>
                <input
                  formControlName="contactEmail"
                  type="email"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('contactEmail')?.invalid && registerForm.get('contactEmail')?.touched }"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Nom de l'entreprise</label>
                <input
                  formControlName="companyName"
                  type="text"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  [ngClass]="{ 'border-red-500': registerForm.get('companyName')?.invalid && registerForm.get('companyName')?.touched }"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700">Secteur d'activité</label>
                <input
                  formControlName="companyIndustrySector"
                  type="text"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>
            </div>

            <!-- Common Password Field -->
            <div>
              <label class="block text-sm font-medium text-gray-700">Mot de passe</label>
              <input
                formControlName="password"
                type="password"
                class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                [ngClass]="{ 'border-red-500': registerForm.get('password')?.invalid && registerForm.get('password')?.touched }"
              />
            </div>

            <!-- Error Message -->
            <div *ngIf="errorMessage" class="p-3 text-sm text-red-700 bg-red-100 border border-red-400 rounded-md">
              {{ errorMessage }}
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              [disabled]="registerForm.invalid || isLoading"
              class="flex justify-center w-full px-4 py-2 text-sm font-medium text-white bg-primary-600 border border-transparent rounded-md shadow-sm hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span *ngIf="!isLoading">S'inscrire</span>
              <span *ngIf="isLoading" class="flex items-center">
                <svg class="w-5 h-5 mr-3 -ml-1 text-white animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Inscription...
              </span>
            </button>
          </form>

          <p class="mt-4 text-sm text-center text-gray-600">
            Déjà un compte ?
            <a routerLink="/auth/login" class="font-medium text-primary-600 hover:text-primary-500">
              Se connecter ici
            </a>
          </p>
        </div>
      </div>
      
      <div class="w-full">
        <div class="w-full h-screen relative bg-amber-200 inset-0 bg-cover bg-center" style="background-image: url('/slider.jpg');">
          <div class="absolute inset-0 overflow-hidden bg-gradient-to-r from-[#2dd4bf] to-[#1f2937] opacity-50"></div>
          <div class="absolute inset-0 overflow-hidden bg-gradient-to-r from-primary-800 via-primary-700 to-primary-900 opacity-90">
            <div class="absolute bottom-0 right-0 w-96 h-96 bg-gradient-to-r bg-[#2dd4bf] to-[#1f2937] rounded-full blur-3xl animate-pulse"></div>
            <div class="absolute bottom-0 right-0 w-64 h-64 bg-orange-400 rounded-full blur-3xl animate-pulse"></div>
            <div class="absolute top-1/2 left-1/3 w-48 h-48 bg-primary-400 rounded-full blur-3xl animate-pulse"></div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  selectedRole: 'STUDENT' | 'COMPANY' | 'FACULTY' = 'STUDENT';
  isLoading = false;
  errorMessage = '';
  schools: School[] = [];
  faculties: Faculty[] = [];
  departments: any[] = [];
  selectedSchoolId: number | null = null;
  selectedFacultyId: number | null = null;
  
  // Arrays pour les noms (pour l'inscription d'établissement)
  schoolNames: string[] = [];
  facultyNames: string[] = [];
  departmentNames: string[] = [];

  constructor(
    private fb: FormBuilder, 
    private schoolService: SchoolService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.registerForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadSchools();
    this.loadNameArrays();
  }

  loadNameArrays(): void {
    console.log('Loading name arrays...');
    
    this.schoolService.getAllSchoolNames().subscribe({
      next: (names) => {
        console.log('School names loaded:', names);
        this.schoolNames = names;
      },
      error: (error) => console.error('Error loading school names:', error)
    });
    
    this.schoolService.getAllFacultyNames().subscribe({
      next: (names) => {
        console.log('Faculty names loaded:', names);
        this.facultyNames = names;
      },
      error: (error) => console.error('Error loading faculty names:', error)
    });
    
    this.schoolService.getAllDepartmentNames().subscribe({
      next: (names) => {
        console.log('Department names loaded:', names);
        this.departmentNames = names;
      },
      error: (error) => console.error('Error loading department names:', error)
    });
  }

  selectRole(role: 'STUDENT' | 'COMPANY' | 'FACULTY') {
    this.selectedRole = role;
    this.registerForm = this.createForm();
  }

  createForm(): FormGroup {
    if (this.selectedRole === 'STUDENT') {
      return this.fb.group({
        firstName: ['', [Validators.required]],
        lastName: ['', [Validators.required]],
        email: ['', [Validators.required, Validators.email]],
        phoneNumber: [''],
        schoolId: ['', [Validators.required]],
        facultyId: ['', [Validators.required]],
        departmentId: ['', [Validators.required]],
        studyLevel: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(6)]]
      });
    } else if (this.selectedRole === 'FACULTY') {
      return this.fb.group({
        contactEmail: ['', [Validators.required, Validators.email]],
        schoolName: ['', [Validators.required]],
        schoolAddress: [''],
        schoolDescription: [''],
        faculties: this.fb.array([this.createFacultyGroup()]),
        password: ['', [Validators.required, Validators.minLength(6)]]
      });
    } else {
      return this.fb.group({
        contactEmail: ['', [Validators.required, Validators.email]],
        companyName: ['', [Validators.required]],
        companyIndustrySector: [''],
        password: ['', [Validators.required, Validators.minLength(6)]]
      });
    }
  }

  loadSchools(): void {
    console.log('Loading schools...');
    this.schoolService.getAllSchools().subscribe({
      next: (schools) => {
        console.log('Schools loaded:', schools);
        this.schools = schools;
      },
      error: (error) => {
        console.error('Error loading schools:', error);
      }
    });
  }

  onSchoolChange(event: any): void {
    const schoolId = parseInt(event.target.value);
    this.selectedSchoolId = schoolId;
    this.faculties = [];
    this.departments = [];
    this.registerForm.get('facultyId')?.setValue('');
    this.registerForm.get('departmentId')?.setValue('');
    
    if (schoolId) {
      this.schoolService.getFacultiesBySchool(schoolId).subscribe({
        next: (faculties) => {
          this.faculties = faculties;
        },
        error: (error) => {
          console.error('Error loading faculties:', error);
        }
      });
    }
  }

  onFacultyChange(event: any): void {
    const facultyId = parseInt(event.target.value);
    this.selectedFacultyId = facultyId;
    this.departments = [];
    this.registerForm.get('departmentId')?.setValue('');
    
    if (facultyId) {
      this.schoolService.getDepartmentsByFaculty(facultyId).subscribe({
        next: (departments) => {
          this.departments = departments;
        },
        error: (error) => {
          console.error('Error loading departments:', error);
        }
      });
    }
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
      let formData = { ...this.registerForm.value };
      
      if (this.selectedRole === 'FACULTY') {
        const facultyNamesString = formData.facultyNames || '';
        formData.facultyNames = facultyNamesString
          .split(',')
          .map((name: string) => name.trim())
          .filter((name: string) => name !== '');
        console.log('Faculty names being sent:', formData.facultyNames);
      }
      
      let registerObservable;
      
      switch (this.selectedRole) {
        case 'STUDENT':
          registerObservable = this.authService.registerStudent(formData);
          break;
        case 'COMPANY':
          registerObservable = this.authService.registerCompany(formData);
          break;
        case 'FACULTY':
          registerObservable = this.authService.registerSchool(formData);
          break;
        default:
          this.isLoading = false;
          return;
      }
      
      registerObservable.subscribe({
        next: (response) => {
          this.isLoading = false;
          this.notificationService.showSuccess('Inscription réussie ! Vous êtes maintenant connecté.');
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          const errorMsg = error.error || 'Erreur lors de l\'inscription';
          this.errorMessage = errorMsg;
          this.notificationService.showError(errorMsg);
        }
      });
    }
  }

  // Méthodes pour la gestion dynamique des facultés
  createFacultyGroup(): FormGroup {
    return this.fb.group({
      name: ['', Validators.required],
      description: [''],
      departments: this.fb.array([this.createDepartmentGroup()])
    });
  }

  createDepartmentGroup(): FormGroup {
    return this.fb.group({
      name: ['', Validators.required],
      headTeacherFirstName: ['', Validators.required],
      headTeacherLastName: ['', Validators.required],
      headTeacherEmail: ['', [Validators.required, Validators.email]]
    });
  }

  getFacultiesArray(): FormArray {
    return this.registerForm.get('faculties') as FormArray;
  }

  getDepartmentsArray(facultyIndex: number): FormArray {
    return this.getFacultiesArray().at(facultyIndex).get('departments') as FormArray;
  }

  addFaculty(): void {
    this.getFacultiesArray().push(this.createFacultyGroup());
  }

  removeFaculty(index: number): void {
    if (this.getFacultiesArray().length > 1) {
      this.getFacultiesArray().removeAt(index);
    }
  }

  addDepartment(facultyIndex: number): void {
    this.getDepartmentsArray(facultyIndex).push(this.createDepartmentGroup());
  }

  removeDepartment(facultyIndex: number, deptIndex: number): void {
    if (this.getDepartmentsArray(facultyIndex).length > 1) {
      this.getDepartmentsArray(facultyIndex).removeAt(deptIndex);
    }
  }
}
