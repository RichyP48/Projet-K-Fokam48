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
      
       <div class="bg-white rounded-lg shadow-sm border  p-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">Conventions en attente de validation</h1>
        <p class="text-primary-600">Validez ou rejetez les conventions de stage de vos étudiants</p>
      </div>

      <div *ngIf="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>

      <div *ngIf="!loading && agreements.length === 0" class="text-center py-8">
        <p class="text-gray-500">Aucune convention en attente de validation</p>
      </div>

      <div *ngIf="!loading && agreements.length > 0" class="space-y-4">
        <div *ngFor="let agreement of agreements" class="bg-white rounded-lg shadow border p-6">
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <h3 class="text-lg font-semibold text-gray-900">
                Convention #{{agreement.id}}
              </h3>
              <p class="text-sm text-gray-600 mt-1">
                Étudiant: {{agreement.studentName}}
              </p>
              <p class="text-sm text-gray-600">
                Entreprise: {{agreement.companyName}}
              </p>
              <p class="text-sm text-gray-600">
                Poste: {{agreement.offerTitle}}
              </p>
              <p class="text-sm text-gray-600">
                Date de création: {{agreement.createdAt | date:'dd/MM/yyyy'}}
              </p>
            </div>
            <div class="flex space-x-2">
              <button *ngIf="agreement.status === 'PENDING_FACULTY_VALIDATION'"
                (click)="validateAgreement(agreement.id, true)"
                class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-sm">
                Valider
              </button>
              <button *ngIf="agreement.status === 'PENDING_FACULTY_VALIDATION'"
                (click)="validateAgreement(agreement.id, false)"
                class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 text-sm">
                Rejeter
              </button>
              <button *ngIf="canFacultySign(agreement)"
                (click)="signAgreement(agreement)"
                class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm">
                Signer
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
        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.showError('Erreur lors du chargement des conventions');
        this.agreements = [];
        this.loading = false;
      }
    });
  }

  validateAgreement(agreementId: number, validated: boolean) {
    if (validated) {
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
    } else {
      this.notificationService.showInfo('Fonctionnalité de rejet à implémenter');
    }
  }

  signAgreement(agreement: any) {
    // TODO: Implémenter la signature par la faculté
    this.notificationService.showInfo('Fonctionnalité de signature à implémenter');
  }

  canFacultySign(agreement: any): boolean {
    return agreement.status === 'PENDING_ADMIN_APPROVAL' && 
           !agreement.signedByFaculty;
  }

}
