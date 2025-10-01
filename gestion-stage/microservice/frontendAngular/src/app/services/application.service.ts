import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Application, ApplicationStatusUpdateRequest } from '../models/application.model';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  constructor(private apiService: ApiService) {}

  /**
   * Submit a new application for an internship offer
   * @param formData Form data containing all application information
   */
  submitApplication(offerId: number, coverLetter: string, cvFile: File): Observable<Application> {
    return this.apiService.submitApplication(offerId, coverLetter, cvFile);
  }

  /**
   * Get applications submitted by the current student
   * @param page Page number
   * @param size Page size
   */
  getStudentApplications(page = 0, size = 10): Observable<any> {
    return this.apiService.getStudentApplications(page, size);
  }

  /**
   * Get applications received by the current company
   * @param page Page number
   * @param size Page size
   * @param offerId Optional filter by offer ID
   */
  getCompanyApplications(page = 0, size = 10, offerId?: number): Observable<any> {
    return this.apiService.getCompanyApplications(page, size, offerId);
  }

  /**
   * Get an application by ID
   * @param applicationId Application ID
   */
  getApplicationById(applicationId: number): Observable<Application> {
    return this.apiService.getApplicationById(applicationId);
  }

  /**
   * Update an application's status (company only)
   * @param applicationId Application ID
   * @param statusData Status update data
   */
  updateApplicationStatus(applicationId: number, statusData: ApplicationStatusUpdateRequest): Observable<Application> {
    return this.apiService.updateApplicationStatus(applicationId, statusData);
  }

  /**
   * Get all applications (admin only)
   * @param page Page number
   * @param size Page size
   */
  getAllApplications(page = 0, size = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'applicationDate,desc');
    
    return this.apiService.get<any>('/admin/applications', params);
  }

  /**
   * Download CV for an application
   * This typically returns a blob for file download
   * @param applicationId Application ID
   */
  downloadCV(applicationId: number): Observable<Blob> {
    return this.apiService.getBlob(`/candidatures/${applicationId}/cv`);
  }
}
