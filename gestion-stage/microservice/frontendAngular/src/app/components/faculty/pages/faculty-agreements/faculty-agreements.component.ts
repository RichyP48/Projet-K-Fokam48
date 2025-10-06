import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConventionService } from '../../../../services/convention.service';
import { NotificationService } from '../../../../services/notification.service';

@Component({
  selector: 'app-faculty-agreements',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <div class="bg-white rounded-lg shadow-sm border p-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">Conventions de stage</h1>
        <p class="text-primary-600">Validez et suivez les conventions de vos étudiants</p>
      </div>

      <!-- Filtres -->
      <div class="bg-white rounded-lg shadow-sm border p-4">
        <div class="flex flex-wrap gap-4">
          <button (click)="filterByStatus('ALL')" 
                  [class]="selectedFilter === 'ALL' ? 'px-4 py-2 bg-primary-600 text-white rounded-md' : 'px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300'">
            Toutes ({{agreements.length}})
          </button>
          <button (click)="filterByStatus('PENDING_FACULTY_VALIDATION')" 
                  [class]="selectedFilter === 'PENDING_FACULTY_VALIDATION' ? 'px-4 py-2 bg-yellow-600 text-white rounded-md' : 'px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300'">
            En attente ({{countByStatus('PENDING_FACULTY_VALIDATION')}})
          </button>
          <button (click)="filterByStatus('PENDING_ADMIN_APPROVAL')" 
                  [class]="selectedFilter === 'PENDING_ADMIN_APPROVAL' ? 'px-4 py-2 bg-blue-600 text-white rounded-md' : 'px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300'">
            Validées ({{countByStatus('PENDING_ADMIN_APPROVAL')}})
          </button>
          <button (click)="filterByStatus('APPROVED')" 
                  [class]="selectedFilter === 'APPROVED' ? 'px-4 py-2 bg-green-600 text-white rounded-md' : 'px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300'">
            Approuvées ({{countByStatus('APPROVED')}})
          </button>
        </div>
      </div>

      <div *ngIf="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>

      <div *ngIf="!loading && filteredAgreements.length === 0" class="text-center py-8">
        <p class="text-gray-500">Aucune convention trouvée</p>
      </div>

      <!-- Cartes de conventions -->
      <div *ngIf="!loading && filteredAgreements.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div *ngFor="let agreement of filteredAgreements" class="bg-white rounded-lg shadow-md border hover:shadow-lg transition-shadow">
          <div class="p-6">
            <div class="flex justify-between items-start mb-4">
              <h3 class="text-lg font-semibold text-gray-900">Convention #{{agreement.id}}</h3>
              <span [ngClass]="getStatusClass(agreement.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                {{getStatusLabel(agreement.status)}}
              </span>
            </div>
            
            <div class="space-y-2 mb-4">
              <p class="text-sm text-gray-600">
                <span class="font-medium">Étudiant:</span> {{agreement.studentName}}
              </p>
              <p class="text-sm text-gray-600">
                <span class="font-medium">Poste:</span> {{agreement.offerTitle}}
              </p>
            </div>

            <!-- Statut des signatures -->
            <div class="mb-4 pb-4 border-b">
              <p class="text-xs font-medium text-gray-700 mb-2">Signatures</p>
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <svg [ngClass]="agreement.signedByStudent ? 'text-green-500' : 'text-gray-300'" class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                  <span class="text-xs text-gray-600">Étudiant</span>
                </div>
                <div class="flex items-center">
                  <svg [ngClass]="agreement.signedByCompany ? 'text-green-500' : 'text-gray-300'" class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                  <span class="text-xs text-gray-600">Entreprise</span>
                </div>
                <div class="flex items-center">
                  <svg [ngClass]="agreement.signedByFaculty ? 'text-green-500' : 'text-gray-300'" class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                  <span class="text-xs text-gray-600">Faculté</span>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex flex-col space-y-2">
              <button *ngIf="agreement.status === 'PENDING_FACULTY_VALIDATION'"
                (click)="validateAgreement(agreement.id)"
                class="w-full px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-sm font-medium">
                ✓ Valider
              </button>
              <button (click)="downloadPDF(agreement)"
                class="w-full px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 text-sm font-medium">
                📄 Télécharger PDF
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class FacultyAgreementsComponent implements OnInit {
  agreements: any[] = [];
  filteredAgreements: any[] = [];
  selectedFilter: string = 'ALL';
  loading = true;

  constructor(
    private conventionService: ConventionService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadPendingAgreements();
  }

  loadPendingAgreements() {
    this.loading = true;
    this.conventionService.getTeacherConventions(0, 50).subscribe({
      next: (response: any) => {
        this.agreements = response || [];
        this.filterByStatus(this.selectedFilter);
        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.showError('Erreur lors du chargement des conventions');
        this.agreements = [];
        this.filteredAgreements = [];
        this.loading = false;
      }
    });
  }

  filterByStatus(status: string) {
    this.selectedFilter = status;
    if (status === 'ALL') {
      this.filteredAgreements = this.agreements;
    } else {
      this.filteredAgreements = this.agreements.filter(a => a.status === status);
    }
  }

  countByStatus(status: string): number {
    return this.agreements.filter(a => a.status === status).length;
  }

  validateAgreement(agreementId: number) {
    const enseignantId = 2; // TODO: Récupérer depuis le JWT
    this.conventionService.validateConvention(agreementId, enseignantId).subscribe({
      next: () => {
        this.notificationService.showSuccess('Convention validée avec succès');
        this.loadPendingAgreements();
      },
      error: () => {
        this.notificationService.showError('Erreur lors de la validation');
      }
    });
  }

  downloadPDF(agreement: any) {
    this.conventionService.downloadConventionPdf(agreement.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `convention-${agreement.id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.notificationService.showError('Erreur lors du téléchargement');
      }
    });
  }

  getStatusClass(status: string): string {
    const classes = {
      'PENDING_FACULTY_VALIDATION': 'bg-yellow-100 text-yellow-800',
      'PENDING_ADMIN_APPROVAL': 'bg-blue-100 text-blue-800',
      'APPROVED': 'bg-green-100 text-green-800',
      'SIGNED': 'bg-purple-100 text-purple-800'
    };
    return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels = {
      'PENDING_FACULTY_VALIDATION': 'En attente',
      'PENDING_ADMIN_APPROVAL': 'Validée',
      'APPROVED': 'Approuvée',
      'SIGNED': 'Signée'
    };
    return labels[status as keyof typeof labels] || status;
  }

}
