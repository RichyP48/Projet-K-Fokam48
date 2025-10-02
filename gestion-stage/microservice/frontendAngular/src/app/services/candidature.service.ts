import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class CandidatureService {
  constructor(private apiService: ApiService) {}

  /**
   * Get candidatures for current company
   */
  getCompanyCandidatures(): Observable<any[]> {
    return this.apiService.get<any[]>('/users/company/applications');
  }

  /**
   * Accept a candidature
   */
  acceptCandidature(candidatureId: number): Observable<any> {
    return this.apiService.put(`/candidatures/${candidatureId}/accept`, {});
  }

  /**
   * Reject a candidature
   */
  rejectCandidature(candidatureId: number, reason: string): Observable<any> {
    return this.apiService.put(`/candidatures/${candidatureId}/reject`, { reason });
  }

  /**
   * Download CV
   */
  downloadCV(candidatureId: number): Observable<Blob> {
    return this.apiService.getBlob(`/candidatures/${candidatureId}/cv`);
  }
}