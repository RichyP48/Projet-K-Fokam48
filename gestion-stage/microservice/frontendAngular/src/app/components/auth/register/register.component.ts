import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule, FormArray } from '@angular/forms';
import { SchoolService, School, Faculty } from '../../../services/school.service';
import { AcademicService, SchoolDropdown, FacultyDropdown, DepartmentDropdown } from '../../../services/academic.service';
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
                Enseignant
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
              <!-- Progress Bar -->
              <div class="mb-6">
                <div class="flex justify-between text-sm text-gray-600 mb-2">
                  <span [class]="currentStep >= 1 ? 'text-primary-600 font-medium' : ''">Informations personnelles</span>
                  <span [class]="currentStep >= 2 ? 'text-primary-600 font-medium' : ''">Informations établissement</span>
                  <span [class]="currentStep >= 3 ? 'text-primary-600 font-medium' : ''">Structure académique</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div class="bg-primary-600 h-2 rounded-full transition-all duration-300" [style.width.%]="(currentStep / 3) * 100"></div>
                </div>
              </div>

              <!-- Step 1: Personal Information -->
              <div *ngIf="currentStep === 1">
                <h3 class="text-lg font-medium text-gray-900 mb-4">Informations personnelles</h3>
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
                    formControlName="contactEmail"
                    type="email"
                    class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    [ngClass]="{ 'border-red-500': registerForm.get('contactEmail')?.invalid && registerForm.get('contactEmail')?.touched }"
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
              </div>

              <!-- Step 2: Academic Assignment -->
              <div *ngIf="currentStep === 2">
                <h3 class="text-lg font-medium text-gray-900 mb-4">Affectation académique</h3>
                <div>
                  <label class="block text-sm font-medium text-gray-700">École</label>
                  <select
                    formControlName="schoolId"
                    class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    [ngClass]="{ 'border-red-500': registerForm.get('schoolId')?.invalid && registerForm.get('schoolId')?.touched }"
                    (change)="onSchoolChange($event)">
                    <option value="">Sélectionnez une école</option>
                    <option *ngFor="let school of schools" [value]="school.id">{{school.name}}</option>
                    <option value="new">+ Ajouter une nouvelle école</option>
                  </select>
                  <div *ngIf="registerForm.get('schoolId')?.value === 'new'" class="mt-2">
                    <input
                      formControlName="newSchoolName"
                      type="text"
                      placeholder="Nom de la nouvelle école"
                      class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                    <input
                      formControlName="newSchoolAddress"
                      type="text"
                      placeholder="Adresse de l'école (optionnel)"
                      class="block w-full px-3 py-2 mt-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>
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
              </div>

              <!-- Step 3: Additional Information -->
              <div *ngIf="currentStep === 3">
                <h3 class="text-lg font-medium text-gray-900 mb-4">Informations complémentaires</h3>
                <div>
                  <label class="block text-sm font-medium text-gray-700">Spécialité</label>
                  <input
                    formControlName="specialty"
                    type="text"
                    placeholder="Ex: Intelligence Artificielle, Réseaux..."
                    class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">Biographie (optionnel)</label>
                  <textarea
                    formControlName="bio"
                    rows="3"
                    placeholder="Présentez-vous brièvement..."
                    class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  ></textarea>
                </div>
              </div>

              <!-- Navigation Buttons -->
              <div class="flex justify-between mt-6">
                <button
                  type="button"
                  (click)="previousStep()"
                  [disabled]="currentStep === 1"
                  class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed">
                  Précédent
                </button>
                <button
                  type="button"
                  (click)="nextStep()"
                  [disabled]="currentStep === 3"
                  class="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-md hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed">
                  Suivant
                </button>
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
                <select
                  formControlName="companyIndustrySector"
                  class="block w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  (change)="onIndustrySectorSelect($event)">
                  <option value="">Sélectionnez un secteur</option>
                  <option *ngFor="let sector of industrySectors" [value]="sector">{{sector}}</option>
                  <option value="autre">Autre (saisir manuellement)</option>
                </select>
                <input
                  *ngIf="registerForm.get('companyIndustrySector')?.value === 'autre'"
                  type="text"
                  placeholder="Saisissez votre secteur d'activité"
                  class="block w-full px-3 py-2 mt-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  (input)="updateIndustrySector($event)"
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
              [disabled]="registerForm.invalid || isLoading || (selectedRole === 'FACULTY' && currentStep < 3)"
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
  schools: SchoolDropdown[] = [];
  faculties: FacultyDropdown[] = [];
  departments: DepartmentDropdown[] = [];
  selectedSchoolId: number | null = null;
  selectedFacultyId: number | null = null;
  currentStep: number = 1;
  
  // Arrays pour les noms (pour l'inscription d'établissement)
  schoolNames: string[] = [];
  facultyNames: string[] = [];
  departmentNames: string[] = [];
  
  // Secteurs d'activité pour les entreprises
  industrySectors: string[] = [
    'Technologies de l\'information',
    'Finance et Banque',
    'Santé et Médecine',
    'Education',
    'Commerce et Vente',
    'Industrie manufacturière',
    'Construction et BTP',
    'Transport et Logistique',
    'Agriculture et Agroalimentaire',
    'Tourisme et Hôtellerie',
    'Médias et Communication',
    'Energie et Environnement',
    'Conseil et Services',
    'Recherche et Développement'
  ];

  constructor(
    private fb: FormBuilder, 
    private schoolService: SchoolService,
    private academicService: AcademicService,
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
    // Charger les noms d'écoles depuis le backend
    this.academicService.getSchoolDropdowns().subscribe({
      next: (schools) => {
        this.schoolNames = schools.map(school => school.name);
      },
      error: () => {
        this.schoolNames = ['Université de Yaoundé I', 'Université de Douala', 'Université de Dschang'];
      }
    });
    
    // Charger les noms de facultés depuis le backend
    this.academicService.getAllFacultyNames().subscribe({
      next: (facultyNames) => {
        this.facultyNames = facultyNames;
      },
      error: () => {
        this.facultyNames = ['Faculté des Sciences', 'Faculté de Médecine', 'École Normale Supérieure', 'Faculté des Sciences Économiques', 'Faculté des Lettres', 'Génie Informatique', 'Génie Civil'];
      }
    });
    
    // Charger les noms de départements depuis le backend
    this.academicService.getAllDepartmentNames().subscribe({
      next: (departmentNames) => {
        this.departmentNames = departmentNames;
      },
      error: () => {
        this.departmentNames = ['Informatique', 'Mathématiques', 'Physique', 'Médecine Générale', 'Chirurgie', 'Enseignement Primaire', 'Économie', 'Gestion', 'Français', 'Génie Logiciel', 'Bâtiment et Travaux Publics'];
      }
    });
  }

  selectRole(role: 'STUDENT' | 'COMPANY' | 'FACULTY') {
    this.selectedRole = role;
    this.currentStep = 1;
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
        firstName: ['', [Validators.required]],
        lastName: ['', [Validators.required]],
        contactEmail: ['', [Validators.required, Validators.email]],
        phoneNumber: [''],
        schoolId: ['', [Validators.required]],
        newSchoolName: [''],
        newSchoolAddress: [''],
        facultyId: ['', [Validators.required]],
        departmentId: ['', [Validators.required]],
        specialty: [''],
        bio: [''],
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
    this.academicService.getSchoolDropdowns().subscribe({
      next: (schools) => {
        console.log('Schools loaded:', schools);
        this.schools = schools || [];
      },
      error: (error) => {
        console.error('Error loading schools:', error);
        this.schools = [
          { id: 1, name: 'Université de Yaoundé I' },
          { id: 2, name: 'Université de Douala' },
          { id: 3, name: 'Université de Dschang' }
        ];
        this.notificationService.showError('Utilisation des données par défaut - Vérifiez la connexion au serveur');
      }
    });
  }

  onSchoolChange(event: any): void {
    const schoolValue = event.target.value;
    this.faculties = [];
    this.departments = [];
    this.registerForm.get('facultyId')?.setValue('');
    this.registerForm.get('departmentId')?.setValue('');
    
    if (schoolValue === 'new') {
      this.selectedSchoolId = null;
      // Clear faculties and departments for new school
      return;
    }
    
    const schoolId = parseInt(schoolValue);
    this.selectedSchoolId = schoolId;
    
    if (schoolId) {
      this.academicService.getFacultyDropdownsBySchool(schoolId).subscribe({
        next: (faculties) => {
          console.log('Faculties loaded for school', schoolId, ':', faculties);
          this.faculties = faculties;
        },
        error: (error) => {
          console.error('Error loading faculties:', error);
          this.faculties = [
            { id: 1, name: 'Sciences' },
            { id: 2, name: 'Lettres et Sciences Humaines' },
            { id: 3, name: 'Ingénierie' }
          ];
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
      this.academicService.getDepartmentDropdownsByFaculty(facultyId).subscribe({
        next: (departments) => {
          console.log('Departments loaded for faculty', facultyId, ':', departments);
          this.departments = departments;
        },
        error: (error) => {
          console.error('Error loading departments:', error);
          // Fallback data
          this.departments = [
            { id: 1, name: 'Informatique' },
            { id: 2, name: 'Mathématiques' },
            { id: 3, name: 'Génie Civil' }
          ];
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
        // Use custom school name if 'autre' was selected
        if (formData.schoolName === 'autre' && formData.customSchoolName) {
          formData.schoolName = formData.customSchoolName;
        }
        delete formData.customSchoolName;
        
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
          registerObservable = this.authService.registerTeacher(formData);
          break;
        default:
          this.isLoading = false;
          return;
      }
      
      registerObservable.subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.notificationService.showSuccess('Inscription réussie ! Vous êtes maintenant connecté.');
          this.router.navigate(['/dashboard']);
        },
        error: (error: any) => {
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

  // Méthodes de récupération lors de la sélection
  onFacultyNameSelect(event: any, facultyIndex: number): void {
    const selectedFacultyName = event.target.value;
    if (selectedFacultyName) {
      console.log(`Faculté sélectionnée: ${selectedFacultyName} à l'index ${facultyIndex}`);
      // Ici vous pouvez ajouter une logique pour récupérer des informations supplémentaires
      // sur la faculté sélectionnée si nécessaire
    }
  }

  onDepartmentNameSelect(event: any, facultyIndex: number, deptIndex: number): void {
    const selectedDepartmentName = event.target.value;
    if (selectedDepartmentName) {
      console.log(`Département sélectionné: ${selectedDepartmentName} à la faculté ${facultyIndex}, département ${deptIndex}`);
      // Ici vous pouvez ajouter une logique pour récupérer des informations supplémentaires
      // sur le département sélectionné si nécessaire
    }
  }

  // Méthode pour récupérer les données d'une école spécifique
  onSchoolNameSelect(event: any): void {
    const schoolName = event.target.value;
    if (schoolName && schoolName !== 'autre') {
      // Utiliser les données déjà chargées depuis le backend
      const selectedSchool = this.schools.find(school => school.name === schoolName);
      if (selectedSchool) {
        console.log('École sélectionnée:', selectedSchool);
        // Si on a besoin de plus de détails, on peut faire un appel API spécifique
        this.academicService.getSchoolDropdowns().subscribe({
          next: (schools) => {
            const fullSchoolData = schools.find(s => s.name === schoolName);
            if (fullSchoolData) {
              // Ici on pourrait récupérer plus de détails si l'API les fournit
              console.log('Détails complets de l\'école:', fullSchoolData);
            }
          },
          error: (error) => {
            console.error('Erreur lors de la récupération des détails de l\'école:', error);
          }
        });
      }
    }
  }

  // Méthode pour gérer la sélection du secteur d'activité
  onIndustrySectorSelect(event: any): void {
    const sector = event.target.value;
    if (sector && sector !== 'autre') {
      console.log('Secteur d\'activité sélectionné:', sector);
    }
  }

  // Méthodes pour mettre à jour les champs de saisie manuelle

  updateIndustrySector(event: any): void {
    this.registerForm.patchValue({companyIndustrySector: event.target.value});
  }

  // Step navigation methods
  nextStep(): void {
    if (this.currentStep < 3) {
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }
}
