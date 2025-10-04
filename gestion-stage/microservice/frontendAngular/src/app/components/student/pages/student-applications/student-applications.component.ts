import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../../services/api.service';
import { NotificationService } from '../../../../services/notification.service';

@Component({
  selector: 'app-student-applications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="space-y-6">
      <div class="bg-white rounded-lg shadow-sm border  p-6">
        <h1 class="text-2xl font-bold text-primary-900">Mes candidatures</h1>
      </div>
      
      <!-- Status Filter -->
      <div class="bg-white rounded-lg shadow-sm border p-6">
        <div class="flex space-x-2">
          <button (click)="filterByStatus('ALL')" [class]="selectedFilter === 'ALL' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'" class="px-3 py-1 rounded-md text-sm">
            Toutes ({{applications.length}})
          </button>
          <button (click)="filterByStatus('PENDING')" [class]="selectedFilter === 'PENDING' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'" class="px-3 py-1 rounded-md text-sm">
            En attente ({{getCountByStatus(['PENDING', 'POSTULE', 'EN_ATTENTE'])}})
          </button>
          <button (click)="filterByStatus('ACCEPTED')" [class]="selectedFilter === 'ACCEPTED' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'" class="px-3 py-1 rounded-md text-sm">
            Acceptées ({{getCountByStatus(['ACCEPTED', 'ACCEPTE'])}})
          </button>
          <button (click)="filterByStatus('REJECTED')" [class]="selectedFilter === 'REJECTED' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'" class="px-3 py-1 rounded-md text-sm">
            Refusées ({{getCountByStatus(['REJECTED', 'REFUSE'])}})
          </button>
        </div>
      </div>
      
      <div class="bg-white rounded-lg shadow-sm border  p-6">
        <div *ngIf="applications.length === 0" class="text-center py-12">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <h3 class="mt-2 text-sm font-medium text-gray-900">Aucune candidature</h3>
          <p class="mt-1 text-sm text-gray-500">Commencez par postuler à des offres de stage.</p>
          <div class="mt-6">
            <a routerLink="/offers" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700">
              Voir les offres
            </a>
          </div>
        </div>

        <div *ngIf="filteredApplications.length > 0" class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-primary-50">
              <tr>
                <th class="px-4 py-3 text-left text-primary-900">Offre</th>
                <th class="px-4 py-3 text-left text-primary-900">Date de candidature</th>
                <th class="px-4 py-3 text-left text-primary-900">Statut</th>
                <th class="px-4 py-3 text-left text-primary-900">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let application of filteredApplications" class="border-b border-primary-100">
                <td class="px-4 py-3">
                  <div>
                    <p class="font-medium text-primary-900">{{application.offerTitle || 'Offre ID: ' + application.offreId}}</p>
                    <p class="text-sm text-gray-500">{{application.companyName || 'Entreprise inconnue'}}</p>
                    <p class="text-sm text-gray-400">{{application.location || ''}} {{application.duration ? '• ' + application.duration : ''}}</p>
                    <p *ngIf="application.commentaires" class="text-sm text-gray-600 mt-1">{{application.commentaires | slice:0:50}}{{application.commentaires.length > 50 ? '...' : ''}}</p>
                  </div>
                </td>
                <td class="px-4 py-3 text-sm text-gray-600">
                  {{application.datePostulation | date:'dd/MM/yyyy HH:mm'}}
                </td>
                <td class="px-4 py-3">
                  <div>
                    <span [ngClass]="getStatusClass(application.status || application.statut)" class="px-2 py-1 rounded-full text-sm">
                      {{getStatusLabel(application.status || application.statut)}}
                    </span>
                    <div class="mt-1 flex space-x-2">
                      <span *ngIf="application.hasCv" class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-blue-100 text-blue-800">
                        📄 CV
                      </span>
                      <span *ngIf="application.hasLettreMotivation" class="inline-flex items-center px-2 py-0.5 rounded text-xs bg-green-100 text-green-800">
                        📝 Lettre
                      </span>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3">
                  <button (click)="viewDetails(application)" class="text-primary-600 hover:text-primary-800 text-sm">
                    Voir détails
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal des détails de l'offre -->
    <div *ngIf="selectedApplication" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" (click)="closeModal()">
      <div class="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white" (click)="$event.stopPropagation()">
        <div class="mt-3">
          <!-- Header -->
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-lg font-medium text-gray-900">Détails de la candidature</h3>
            <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <!-- Informations de l'offre -->
          <div class="bg-gray-50 rounded-lg p-4 mb-4">
            <h4 class="font-semibold text-lg text-primary-900 mb-2">{{selectedApplication.offerTitle || 'Offre ID: ' + selectedApplication.offreId}}</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <span class="font-medium text-gray-700">🏢 Entreprise:</span>
                <span class="ml-2">{{selectedApplication.companyName || 'Non spécifiée'}}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700">📍 Localisation:</span>
                <span class="ml-2">{{selectedApplication.location || 'Non spécifiée'}}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700">⏰ Durée:</span>
                <span class="ml-2">{{selectedApplication.duration || 'Non spécifiée'}}</span>
              </div>
              <div>
                <span class="font-medium text-gray-700">📅 Candidature:</span>
                <span class="ml-2">{{selectedApplication.datePostulation | date:'dd/MM/yyyy HH:mm'}}</span>
              </div>
            </div>
          </div>

          <!-- Statut et documents -->
          <div class="mb-4">
            <div class="flex items-center justify-between mb-3">
              <span class="font-medium text-gray-700">Statut:</span>
              <span [ngClass]="getStatusClass(selectedApplication.status || selectedApplication.statut)" class="px-3 py-1 rounded-full text-sm">
                {{getStatusLabel(selectedApplication.status || selectedApplication.statut)}}
              </span>
            </div>
            
            <div class="flex items-center space-x-3">
              <span class="font-medium text-gray-700">Documents:</span>
              <span *ngIf="selectedApplication.hasCv" class="inline-flex items-center px-2 py-1 rounded text-sm bg-blue-100 text-blue-800">
                📄 CV jointé
              </span>
              <span *ngIf="selectedApplication.hasLettreMotivation" class="inline-flex items-center px-2 py-1 rounded text-sm bg-green-100 text-green-800">
                📝 Lettre jointée
              </span>
              <span *ngIf="!selectedApplication.hasCv && !selectedApplication.hasLettreMotivation" class="text-gray-500 text-sm">
                Aucun document
              </span>
            </div>
          </div>

          <!-- Commentaires -->
          <div *ngIf="selectedApplication.commentaires" class="mb-6">
            <span class="font-medium text-gray-700">Commentaires:</span>
            <p class="mt-1 text-gray-600 bg-gray-50 p-3 rounded">{{selectedApplication.commentaires}}</p>
          </div>

          <!-- Actions -->
          <div class="flex justify-end space-x-3">
            <button (click)="closeModal()" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50">
              Fermer
            </button>
            <button *ngIf="canAbandon(selectedApplication.status || selectedApplication.statut)" 
                    (click)="abandonApplication(selectedApplication)" 
                    class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700">
              Abandonner la candidature
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``
})
export class StudentApplicationsComponent implements OnInit {
  applications: any[] = [];
  filteredApplications: any[] = [];
  selectedFilter = 'ALL';
  selectedApplication: any = null;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications() {
    this.apiService.getStudentApplications().subscribe({
      next: (response) => {
        this.applications = response.content || response;
        console.log('🎓 Student applications loaded:', this.applications.length);
        console.log('📊 Applications data:', this.applications);
        
        // Debug: Log status of each application
        this.applications.forEach((app, index) => {
          console.log(`🔍 App ${index}: ID=${app.id}`);
          console.log(`   - app.status: "${app.status}"`);
          console.log(`   - app.statut: "${app.statut}"`);
          console.log(`   - OfferTitle: ${app.offerTitle}`);
          console.log(`   - Full app object:`, app);
        });
        
        this.applyFilter();
      },
      error: (error) => {
        console.warn('Service candidatures indisponible:', error.status);
        this.applications = [];
        this.filteredApplications = [];
      }
    });
  }

  getStatusClass(status: string): string {
    const classes = {
      // French statuses
      'POSTULE': 'bg-blue-100 text-blue-800',
      'EN_ATTENTE': 'bg-yellow-100 text-yellow-800',
      'ACCEPTE': 'bg-green-100 text-green-800',
      'REFUSE': 'bg-red-100 text-red-800',
      'RETIRE': 'bg-gray-100 text-gray-800',
      // English statuses
      'PENDING': 'bg-blue-100 text-blue-800',
      'ACCEPTED': 'bg-green-100 text-green-800',
      'REJECTED': 'bg-red-100 text-red-800',
      'WITHDRAWN': 'bg-gray-100 text-gray-800'
    };
    return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels = {
      // French statuses
      'POSTULE': 'Postulée',
      'EN_ATTENTE': 'En attente',
      'ACCEPTE': 'Acceptée',
      'REFUSE': 'Refusée',
      'RETIRE': 'Retirée',
      // English statuses
      'PENDING': 'En attente',
      'ACCEPTED': 'Acceptée',
      'REJECTED': 'Refusée',
      'WITHDRAWN': 'Retirée'
    };
    return labels[status as keyof typeof labels] || status;
  }

  viewDetails(application: any) {
    this.selectedApplication = application;
  }

  closeModal() {
    this.selectedApplication = null;
  }

  canAbandon(status: string): boolean {
    return status === 'POSTULE' || status === 'EN_ATTENTE' || status === 'PENDING';
  }

  abandonApplication(application: any) {
    if (confirm('Êtes-vous sûr de vouloir abandonner cette candidature ?')) {
      this.apiService.updateApplicationStatus(application.id, { 
        event: 'RETIRER',
        commentaire: 'Candidature abandonnée par l\'étudiant',
        userId: application.etudiantId 
      }).subscribe({
        next: () => {
          this.notificationService.showSuccess('Candidature abandonnée avec succès');
          this.closeModal();
          this.loadApplications(); // Recharger la liste
        },
        error: (error) => {
          this.notificationService.showError('Erreur lors de l\'abandon de la candidature');
        }
      });
    }
  }

  filterByStatus(status: string): void {
    this.selectedFilter = status;
    this.applyFilter();
  }

  applyFilter(): void {
    console.log('🔍 Applying filter:', this.selectedFilter);
    console.log('📊 Total applications:', this.applications.length);
    
    if (this.selectedFilter === 'ALL') {
      this.filteredApplications = this.applications;
    } else if (this.selectedFilter === 'PENDING') {
      this.filteredApplications = this.applications.filter(app => 
        ['PENDING', 'POSTULE', 'EN_ATTENTE'].includes(app.status || app.statut)
      );
      console.log('⏳ Pending applications found:', this.filteredApplications.length);
    } else if (this.selectedFilter === 'ACCEPTED') {
      this.filteredApplications = this.applications.filter(app => 
        ['ACCEPTED', 'ACCEPTE'].includes(app.status || app.statut)
      );
      console.log('✅ Accepted applications found:', this.filteredApplications.length);
      this.filteredApplications.forEach(app => {
        console.log(`   - App ${app.id}: ${app.status || app.statut} (${app.offerTitle})`);
      });
    } else if (this.selectedFilter === 'REJECTED') {
      this.filteredApplications = this.applications.filter(app => 
        ['REJECTED', 'REFUSE'].includes(app.status || app.statut)
      );
      console.log('❌ Rejected applications found:', this.filteredApplications.length);
    }
    
    console.log('📋 Filtered applications:', this.filteredApplications.length);
  }

  getCountByStatus(statuses: string[]): number {
    return this.applications.filter(app => statuses.includes(app.status || app.statut)).length;
  }
}
