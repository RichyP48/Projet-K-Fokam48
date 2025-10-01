import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SchoolService, School, Faculty, Department } from '../../services/school.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-academic-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="space-y-6">
      <div class="bg-white rounded-lg shadow-sm border p-6">
        <h1 class="text-2xl font-bold text-primary-900">Structure Académique</h1>
        
        <!-- Filtres -->
        <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
          <input [(ngModel)]="searchTerm" (input)="filterSchools()" 
                 placeholder="Rechercher une école..." 
                 class="border border-gray-300 rounded-md px-3 py-2">
          <select [(ngModel)]="selectedSchoolId" (change)="onSchoolSelect()" 
                  class="border border-gray-300 rounded-md px-3 py-2">
            <option value="">Toutes les écoles</option>
            <option *ngFor="let school of schools" [value]="school.id">{{school.name}}</option>
          </select>
        </div>
      </div>
      
      <!-- Liste des écoles -->
      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <div *ngFor="let school of filteredSchools" class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-xl font-bold text-primary-900">{{ school.name }}</h3>
            <button (click)="viewSchoolDetails(school)" class="text-primary-600 hover:text-primary-800">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
              </svg>
            </button>
          </div>
          
          <div class="space-y-2">
            <p class="text-gray-600" *ngIf="school.address">
              <svg class="w-4 h-4 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
              </svg>
              {{ school.address }}
            </p>
            <p class="text-sm text-gray-500" *ngIf="school.description">{{ school.description }}</p>
          </div>
          
          <div class="mt-4 flex justify-between items-center">
            <span class="text-sm text-gray-500">{{ getFacultyCount(school.id) }} facultés</span>
            <button (click)="loadFaculties(school.id)" 
                    class="bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700 text-sm">
              Voir facultés
            </button>
          </div>
        </div>
      </div>
      
      <div *ngIf="filteredSchools.length === 0" class="text-center py-12">
        <p class="text-gray-500">Aucune école trouvée</p>
      </div>
    </div>

    <!-- Modal détails école -->
    <div *ngIf="selectedSchool" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-4xl max-h-screen overflow-y-auto">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-xl font-semibold">{{ selectedSchool.name }}</h3>
          <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="space-y-6">
          <div *ngIf="selectedSchool.description">
            <h4 class="font-medium text-gray-900">Description</h4>
            <p class="text-gray-600">{{ selectedSchool.description }}</p>
          </div>
          
          <div *ngIf="selectedSchool.address">
            <h4 class="font-medium text-gray-900">Adresse</h4>
            <p class="text-gray-600">{{ selectedSchool.address }}</p>
          </div>
          
          <!-- Facultés -->
          <div>
            <h4 class="font-medium text-gray-900 mb-3">Facultés ({{ schoolFaculties.length }})</h4>
            <div class="grid gap-4 md:grid-cols-2">
              <div *ngFor="let faculty of schoolFaculties" class="border rounded-lg p-4">
                <h5 class="font-medium text-primary-900">{{ faculty.name }}</h5>
                <p class="text-sm text-gray-600 mt-1" *ngIf="faculty.description">{{ faculty.description }}</p>
                <div class="mt-2">
                  <button (click)="loadDepartments(faculty.id)" 
                          class="text-primary-600 hover:text-primary-800 text-sm">
                    Voir départements ({{ getDepartmentCount(faculty.id) }})
                  </button>
                </div>
                
                <!-- Départements -->
                <div *ngIf="facultyDepartments[faculty.id]" class="mt-3 pl-4 border-l-2 border-gray-200">
                  <div *ngFor="let dept of facultyDepartments[faculty.id]" class="py-1">
                    <span class="text-sm text-gray-700">{{ dept.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="flex justify-end mt-6">
          <button (click)="closeModal()" class="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50">
            Fermer
          </button>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class AcademicListComponent implements OnInit {
  schools: School[] = [];
  filteredSchools: School[] = [];
  selectedSchool: School | null = null;
  schoolFaculties: Faculty[] = [];
  facultyDepartments: { [facultyId: number]: Department[] } = {};
  
  searchTerm = '';
  selectedSchoolId = '';

  constructor(
    private schoolService: SchoolService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadSchools();
  }

  loadSchools() {
    this.schoolService.getAllSchools().subscribe({
      next: (data) => {
        this.schools = data;
        this.filteredSchools = [...this.schools];
      },
      error: (error) => {
        console.error('Error loading schools:', error);
        // Fallback data
        this.schools = [
          { id: 1, name: 'Université de Yaoundé I', address: 'Yaoundé, Cameroun', description: 'Première université du Cameroun' },
          { id: 2, name: 'Université de Douala', address: 'Douala, Cameroun', description: 'Université de la capitale économique' },
          { id: 3, name: 'Université de Dschang', address: 'Dschang, Cameroun', description: 'Université de l\'Ouest Cameroun' }
        ];
        this.filteredSchools = [...this.schools];
        this.notificationService.showError('Utilisation des données par défaut - Vérifiez la connexion au serveur');
      }
    });
  }

  filterSchools() {
    this.filteredSchools = this.schools.filter(school =>
      school.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      (school.description && school.description.toLowerCase().includes(this.searchTerm.toLowerCase()))
    );
  }

  onSchoolSelect() {
    if (this.selectedSchoolId) {
      this.filteredSchools = this.schools.filter(school => school.id.toString() === this.selectedSchoolId);
    } else {
      this.filteredSchools = [...this.schools];
    }
  }

  viewSchoolDetails(school: School) {
    this.selectedSchool = school;
    this.loadFaculties(school.id);
  }

  loadFaculties(schoolId: number) {
    this.schoolService.getFacultiesBySchool(schoolId).subscribe({
      next: (faculties) => {
        this.schoolFaculties = faculties;
      },
      error: (error) => {
        console.error('Error loading faculties:', error);
        // Fallback data
        this.schoolFaculties = [
          { id: 1, name: 'Sciences', school: { id: schoolId, name: '' } },
          { id: 2, name: 'Lettres et Sciences Humaines', school: { id: schoolId, name: '' } },
          { id: 3, name: 'Ingénierie', school: { id: schoolId, name: '' } }
        ];
      }
    });
  }

  loadDepartments(facultyId: number) {
    if (this.facultyDepartments[facultyId]) {
      delete this.facultyDepartments[facultyId];
      return;
    }

    this.schoolService.getDepartmentsByFaculty(facultyId).subscribe({
      next: (departments) => {
        this.facultyDepartments[facultyId] = departments;
      },
      error: (error) => {
        console.error('Error loading departments:', error);
        // Fallback data
        this.facultyDepartments[facultyId] = [
          { id: 1, name: 'Informatique', faculty: { id: facultyId, name: '', school: { id: 0, name: '' } } },
          { id: 2, name: 'Mathématiques', faculty: { id: facultyId, name: '', school: { id: 0, name: '' } } },
          { id: 3, name: 'Génie Civil', faculty: { id: facultyId, name: '', school: { id: 0, name: '' } } }
        ];
      }
    });
  }

  getFacultyCount(schoolId: number): number {
    return this.selectedSchool?.id === schoolId ? this.schoolFaculties.length : 0;
  }

  getDepartmentCount(facultyId: number): number {
    return this.facultyDepartments[facultyId]?.length || 0;
  }

  closeModal() {
    this.selectedSchool = null;
    this.schoolFaculties = [];
    this.facultyDepartments = {};
  }
}