import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FacultyService, FacultyStudent } from '../../../../services/faculty.service';

@Component({
  selector: 'app-faculty-students',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-6">
        <a routerLink="/enseignant/dashboard" class="inline-flex items-center text-blue-600 hover:text-blue-800">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Retour au tableau de bord
        </a>
      </div>
      
      <h1 class="text-3xl font-bold text-gray-900 mb-6">Mes Étudiants</h1>
      
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <div *ngIf="loading" class="p-6 text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p class="mt-2 text-gray-600">Chargement des étudiants...</p>
        </div>
        
        <div *ngIf="!loading && students.length === 0" class="p-6 text-center">
          <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
          </svg>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucun étudiant enregistré</h3>
          <p class="text-gray-600">Aucun étudiant n'est actuellement enregistré dans votre département.</p>
          <p class="text-sm text-gray-500 mt-2">Vérifiez que votre filière est correctement configurée dans votre profil.</p>
        </div>
        
        <div *ngIf="!loading && students.length > 0">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Étudiants de mon département ({{students.length}})</h3>
          </div>
          
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Département</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Téléphone</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr *ngFor="let student of students" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-medium text-gray-900">{{student.prenom}} {{student.nom}}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{student.email}}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded-full">
                      {{student.department || 'Non spécifié'}}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{student.telephone || 'Non renseigné'}}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 py-1 text-xs font-medium rounded-full" 
                          [ngClass]="student.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                      {{student.active ? 'Actif' : 'Inactif'}}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class FacultyStudentsComponent implements OnInit {
  students: FacultyStudent[] = [];
  loading = true;

  constructor(private facultyService: FacultyService) {}

  ngOnInit() {
    this.loadStudents();
  }

  loadStudents() {
    this.loading = true;
    this.facultyService.getMyStudents().subscribe({
      next: (students) => {
        console.log('✅ Étudiants récupérés:', students);
        this.students = students || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des étudiants:', error);
        this.students = [];
        this.loading = false;
      }
    });
  }
}
