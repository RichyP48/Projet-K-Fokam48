import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FacultyService } from '../../../services/faculty.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-faculty-students',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <div class="bg-white rounded-lg shadow-sm border  p-6">
        <h1 class="text-2xl font-bold text-primary-900 mb-2">Liste des Étudiants</h1>
        <p class="text-primary-600">{{students.length}} étudiants trouvés</p>
      </div>

      <!-- Loading -->
      <div *ngIf="loading" class="bg-white rounded-lg shadow-sm border  p-8 text-center">
        <p>🔄 Chargement des étudiants...</p>
      </div>

      <!-- Error -->
      <div *ngIf="error" class="bg-red-50 border border-red-200 rounded-lg p-6">
        <p class="text-red-800">❌ {{error}}</p>
        <button (click)="loadStudents()" class="mt-2 bg-red-600 text-white px-4 py-2 rounded">
          Réessayer
        </button>
      </div>

      <!-- Students List -->
      <div *ngIf="!loading && !error" class="bg-white rounded-lg shadow-sm border ">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-primary-900">Étudiants ({{students.length}})</h2>
        </div>

        <div *ngIf="students.length === 0" class="p-8 text-center">
          <p class="text-gray-500">👥 Aucun étudiant trouvé</p>
        </div>

        <div *ngIf="students.length > 0" class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div *ngFor="let student of students" class="border border-gray-200 rounded-lg p-4">
              <div class="flex items-center">
                <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                  <span class="text-white font-bold text-sm">
                    {{(student.prenom?.charAt(0) || '') + (student.nom?.charAt(0) || '')}}
                  </span>
                </div>
                <div class="ml-3">
                  <h3 class="font-semibold text-gray-900">{{student.prenom}} {{student.nom}}</h3>
                  <p class="text-gray-600 text-sm">{{student.email}}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class FacultyStudentsComponent implements OnInit {
  students: any[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private facultyService: FacultyService
  ) {
    console.log('🎓 FacultyStudentsComponent initialized');
  }

  ngOnInit(): void {
    console.log('📚 Loading students...');
    this.loadStudents();
  }

  loadStudents(): void {
    this.loading = true;
    this.error = null;

    // Test d'abord l'endpoint de test
    this.facultyService.testAccess().subscribe({
      next: (testResult) => {
        console.log('Test endpoint works:', testResult);
        // Maintenant essayer de charger les étudiants
        this.facultyService.getMyStudents().subscribe({
          next: (students) => {
            this.students = students || [];
            this.loading = false;
          },
          error: (error) => {
            console.error('Erreur lors du chargement des étudiants:', error);
            this.error = error.error?.message || error.message || 'Erreur lors du chargement des étudiants';
            this.students = [];
            this.loading = false;
          }
        });
      },
      error: (testError) => {
        console.error('Test endpoint failed:', testError);
        this.error = 'Problème d\'authentification ou d\'autorisation';
        this.loading = false;
      }
    });
  }


}
