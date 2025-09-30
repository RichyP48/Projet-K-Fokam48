import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ConventionService } from '../../../../services/convention.service';
import { NotificationService } from '../../../../services/notification.service';

@Component({
  selector: 'app-admin-agreements',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-6">
        <a routerLink="/admin" class="inline-flex items-center text-primary-600 hover:text-primary-800">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Retour au tableau de bord
        </a>
      </div>
      
      <h1 class="text-3xl font-bold text-gray-900 mb-6">Toutes les conventions</h1>
      
      <div class="mb-4 flex space-x-2">
        <button (click)="filterType = 'all'; loadConventions()" 
                [ngClass]="filterType === 'all' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'"
                class="px-4 py-2 rounded-md text-sm">
          Toutes ({{totalCount}})
        </button>
        <button (click)="filterType = 'pending'; loadConventions()" 
                [ngClass]="filterType === 'pending' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'"
                class="px-4 py-2 rounded-md text-sm">
          En attente ({{pendingCount}})
        </button>
      </div>
      
      <div *ngIf="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
      
      <div *ngIf="!loading" class="space-y-4">
        <div *ngIf="conventions.length === 0" class="bg-white rounded-lg shadow p-8 text-center">
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucune convention</h3>
          <p class="text-gray-500">Aucune convention trouvée.</p>
        </div>
        
        <div *ngFor="let convention of conventions" class="bg-white rounded-lg shadow-md p-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-lg font-semibold text-gray-900">{{convention.offerTitle}}</h3>
              <p class="text-sm text-gray-600">Étudiant: {{convention.studentName}}</p>
              <p class="text-sm text-gray-600">Entreprise: {{convention.companyName}}</p>
              <p class="text-sm text-gray-600">Enseignant: {{convention.teacherName}}</p>
            </div>
            <span [ngClass]="getStatusClass(convention.status)" class="px-3 py-1 rounded-full text-sm">
              {{getStatusLabel(convention.status)}}
            </span>
          </div>
          
          <div class="mb-4">
            <p class="text-sm font-medium text-gray-700 mb-2">État du processus</p>
            <div class="grid grid-cols-2 gap-4">
              <div class="flex items-center">
                <svg [ngClass]="convention.signedByStudent ? 'text-green-500' : 'text-gray-300'" class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                <span class="text-sm text-gray-600">Étudiant</span>
              </div>
              <div class="flex items-center">
                <svg [ngClass]="convention.signedByCompany ? 'text-green-500' : 'text-gray-300'" class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                <span class="text-sm text-gray-600">Entreprise</span>
              </div>
              <div class="flex items-center">
                <svg [ngClass]="convention.signedByFaculty ? 'text-green-500' : 'text-gray-300'" class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                <span class="text-sm text-gray-600">Faculté</span>
              </div>
              <div class="flex items-center">
                <svg [ngClass]="convention.approvedByAdmin ? 'text-green-500' : 'text-gray-300'" class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                <span class="text-sm text-gray-600">Mon approbation</span>
              </div>
            </div>
          </div>
          
          <div class="flex space-x-2">
            <button (click)="downloadPDF(convention)" class="bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700 text-sm">
              Télécharger PDF
            </button>
            <button *ngIf="!convention.approvedByAdmin" 
                    (click)="approveConvention(convention)" 
                    class="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 text-sm">
              Approuver
            </button>
            <button (click)="viewDetails(convention)" 
                    class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 text-sm">
              Détails
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AdminAgreementsComponent implements OnInit {
  conventions: any[] = [];
  loading = false;
  filterType: 'all' | 'pending' = 'all';
  totalCount = 0;
  pendingCount = 0;

  constructor(
    private conventionService: ConventionService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadConventions();
  }

  loadConventions(): void {
    this.loading = true;
    
    if (this.filterType === 'pending') {
      this.conventionService.getPendingConventions(0, 50).subscribe({
        next: (response: any) => {
          this.conventions = response || [];
          this.pendingCount = this.conventions.length;
          this.loading = false;
        },
        error: () => {
          this.notificationService.showError('Erreur lors du chargement des conventions en attente');
          this.loading = false;
        }
      });
    } else {
      this.conventionService.getAdminConventions(0, 50).subscribe({
        next: (response: any) => {
          this.conventions = response || [];
          this.totalCount = this.conventions.length;
          this.pendingCount = this.conventions.filter(c => !c.approvedByAdmin).length;
          this.loading = false;
        },
        error: () => {
          this.notificationService.showError('Erreur lors du chargement des conventions');
          this.loading = false;
        }
      });
    }
  }

  approveConvention(convention: any): void {
    // TODO: Implémenter l'approbation admin
    convention.approvedByAdmin = true;
    this.notificationService.showSuccess('Convention approuvée avec succès');
    this.loadConventions();
  }

  viewDetails(convention: any): void {
    // TODO: Ouvrir modal ou page de détails
    this.notificationService.showInfo('Fonctionnalité de détails à implémenter');
  }

  downloadPDF(convention: any): void {
    this.conventionService.downloadConventionPdf(convention.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `convention-${convention.id}.pdf`;
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
      'REJECTED': 'bg-red-100 text-red-800'
    };
    return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels = {
      'PENDING_FACULTY_VALIDATION': 'En attente validation',
      'PENDING_ADMIN_APPROVAL': 'En attente approbation',
      'APPROVED': 'Approuvée',
      'REJECTED': 'Rejetée'
    };
    return labels[status as keyof typeof labels] || status;
  }
}