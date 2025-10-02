import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ApplicationService } from '../../../../services/application.service';
import { NotificationService } from '../../../../services/notification.service';
import { AgreementService } from '../../../../services/agreement.service';
import { ApplicationStatus } from '../../../../models/application.model';

@Component({
  selector: 'app-company-applications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-6">
        <a routerLink="/company" class="inline-flex items-center text-primary-600 hover:text-primary-800">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Dashboard
        </a>
      </div>
      
      <h1 class="text-3xl font-bold text-gray-900 mb-6">Candidatures reçues..</h1>
      
      <!-- Status Filter -->
      <div class="mb-4">
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
      
      <!-- Loading -->
      <div *ngIf="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
      
      <!-- Applications List -->
      <div *ngIf="!loading" class="space-y-4">
        <div *ngIf="filteredApplications.length === 0" class="bg-white rounded-lg shadow p-8 text-center">
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucune candidature</h3>
          <p class="text-gray-500">Aucune candidature n'a été reçue pour vos offres.</p>
        </div>
        
        <div *ngFor="let application of filteredApplications" class="bg-white rounded-lg shadow-md p-6">
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <h3 class="text-lg font-semibold text-gray-900">{{application.studentName}}</h3>
              <p class="text-gray-600 mb-2">{{application.offerTitle}}</p>
              <p class="text-sm text-gray-500 mb-4">{{application.coverLetter}}</p>
              <p class="text-xs text-blue-500 mb-2">ID: {{application.id}} | Offre: {{application.offreId}}</p>
              <div class="flex items-center space-x-4 text-sm text-gray-500">
                <span>Candidature du {{formatDate(application.applicationDate)}}</span>
                <span [ngClass]="getStatusClass(application.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                  {{getStatusLabel(application.status)}}
                </span>
              </div>
            </div>
            <div class="flex space-x-2 ml-4">
              <button *ngIf="application.cvPath" (click)="downloadCV(application)" class="px-3 py-1.5 text-xs font-medium text-primary-600 border border-primary-600 rounded-md hover:bg-primary-50">
                Télécharger CV
              </button>
              <button *ngIf="application.status === 'PENDING' || application.status === 'POSTULE' || application.status === 'EN_ATTENTE'"
                      (click)="updateStatus(application, ApplicationStatus.ACCEPTED)" 
                      class="px-3 py-1.5 text-xs font-medium text-white bg-green-600 rounded-md hover:bg-green-700">
                Accepter
              </button>
              <button *ngIf="application.status === 'PENDING' || application.status === 'POSTULE' || application.status === 'EN_ATTENTE'"
                      (click)="updateStatus(application, ApplicationStatus.REJECTED)" 
                      class="px-3 py-1.5 text-xs font-medium text-white bg-red-600 rounded-md hover:bg-red-700">
                Refuser
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class CompanyApplicationsComponent implements OnInit {
  applications: any[] = [];
  filteredApplications: any[] = [];
  selectedFilter = 'ALL';
  loading = false;
  offerId: number | null = null;
  ApplicationStatus = ApplicationStatus;

  constructor(
    private applicationService: ApplicationService,
    private notificationService: NotificationService,
    private agreementService: AgreementService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.offerId = params['offerId'] ? parseInt(params['offerId']) : null;
      this.loadApplications();
    });
  }

  loadApplications(): void {
    this.loading = true;
    
    const userId = localStorage.getItem('user_id');
    console.log('🏢 Company', userId, 'loading applications...');
    
    this.applicationService.getCompanyApplications(0, 50, this.offerId || undefined).subscribe({
      next: (response: any) => {
        this.applications = response.content || response || [];
        console.log('📝 Company', userId, 'loaded', this.applications.length, 'applications');
        console.log('🔍 Applications data:', this.applications);
        
        // Debug: Log status of each application
        this.applications.forEach((app, index) => {
          console.log(`App ${index}: ID=${app.id}, Status=${app.status}, OfferTitle=${app.offerTitle}`);
        });
        
        this.applyFilter();
        this.loading = false;
      },
      error: (error: any) => {
        console.error('❌ Error loading applications:', error);
        this.notificationService.showError('Erreur lors du chargement des candidatures');
        this.loading = false;
      }
    });
  }

  updateStatus(application: any, status: ApplicationStatus): void {
    const endpoint = status === ApplicationStatus.ACCEPTED ? 'accept' : 'reject';
    const apiCall = status === ApplicationStatus.ACCEPTED 
      ? this.applicationService.acceptApplication(application.id)
      : this.applicationService.rejectApplication(application.id, 'Application rejected by company');
    
    apiCall.subscribe({
      next: (updatedApplication: any) => {
        console.log('🔄 Backend response:', updatedApplication);
        // Use the English status from the DTO
        application.status = updatedApplication.status;
        console.log('🔄 Updated application status to:', application.status);
        
        if (status === ApplicationStatus.ACCEPTED) {
          this.createAgreement(application);
        }
        
        this.notificationService.showSuccess(`Candidature ${status === ApplicationStatus.ACCEPTED ? 'acceptée' : 'refusée'}`);
        
        // Refresh the filter to show updated status immediately
        this.applyFilter();
      },
      error: (error: any) => {
        console.error('Error updating status:', error);
        this.notificationService.showError('Erreur lors de la mise à jour');
      }
    });
  }

  private createAgreement(application: any): void {
    const agreementData = {
      applicationId: application.id,
      studentId: application.studentId,
      companyId: application.companyId,
      offerId: application.offerId
    };

    this.agreementService.createAgreement(agreementData).subscribe({
      next: (agreement: any) => {
        console.log('Convention créée:', agreement);
        this.notificationService.showSuccess('Convention de stage créée automatiquement');
      },
      error: (error: any) => {
        console.error('Erreur lors de la création de la convention:', error);
        this.notificationService.showError('Erreur lors de la création de la convention');
      }
    });
  }

  downloadCV(application: any): void {
    this.applicationService.downloadCV(application.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `CV_${application.studentName}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Error downloading CV:', error);
        this.notificationService.showError('Erreur lors du téléchargement du CV');
      }
    });
  }

  getStatusClass(status: string): string {
    const classes = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'POSTULE': 'bg-blue-100 text-blue-800',
      'EN_ATTENTE': 'bg-yellow-100 text-yellow-800',
      'ACCEPTED': 'bg-green-100 text-green-800',
      'ACCEPTE': 'bg-green-100 text-green-800',
      'REJECTED': 'bg-red-100 text-red-800',
      'REFUSE': 'bg-red-100 text-red-800',
      'RETIRE': 'bg-gray-100 text-gray-800'
    };
    return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labels = {
      'PENDING': 'En attente',
      'POSTULE': 'Postulé',
      'EN_ATTENTE': 'En attente',
      'ACCEPTED': 'Acceptée',
      'ACCEPTE': 'Acceptée',
      'REJECTED': 'Refusée',
      'REFUSE': 'Refusée',
      'RETIRE': 'Retiré'
    };
    return labels[status as keyof typeof labels] || status;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('fr-FR', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    }).format(date);
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
        ['PENDING', 'POSTULE', 'EN_ATTENTE'].includes(app.status)
      );
    } else if (this.selectedFilter === 'ACCEPTED') {
      this.filteredApplications = this.applications.filter(app => 
        ['ACCEPTED', 'ACCEPTE'].includes(app.status)
      );
      console.log('✅ Accepted applications found:', this.filteredApplications.length);
      this.filteredApplications.forEach(app => {
        console.log(`   - App ${app.id}: ${app.status} (${app.offerTitle})`);
      });
    } else if (this.selectedFilter === 'REJECTED') {
      this.filteredApplications = this.applications.filter(app => 
        ['REJECTED', 'REFUSE'].includes(app.status)
      );
      console.log('❌ Rejected applications found:', this.filteredApplications.length);
    }
    
    console.log('📋 Filtered applications:', this.filteredApplications.length);
  }

  getCountByStatus(statuses: string[]): number {
    return this.applications.filter(app => statuses.includes(app.status)).length;
  }
}
