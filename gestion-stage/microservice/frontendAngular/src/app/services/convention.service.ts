import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ConventionService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Generate convention
  generateConvention(request: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/conventions/generate`, request);
  }

  // Get convention PDF
  getConventionPdf(conventionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/conventions/${conventionId}/pdf`, { responseType: 'blob' });
  }

  // Validate convention (faculty)
  validateConvention(conventionId: number, enseignantId: number): Observable<any> {
    const params = new HttpParams().set('enseignantId', enseignantId.toString());
    return this.http.put(`${this.apiUrl}/conventions/${conventionId}/validate`, {}, { params });
  }

  // Sign convention
  signConvention(conventionId: number, signRequest: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/conventions/${conventionId}/sign`, signRequest);
  }



  // Get all conventions (admin)
  getAllConventions(page = 0, size = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(`${this.apiUrl}/conventions`, { params });
  }

  // Get pending conventions (admin)
  getPendingConventions(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get(`${this.apiUrl}/conventions/pending`, { params });
  }

  // Role-based methods
  getCompanyConventions(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions/entreprise`);
  }

  getStudentConventions(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions/etudiant`);
  }

  getTeacherConventions(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions/enseignant`);
  }

  getAdminConventions(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions`);
  }

  // Signature methods
  signConventionAsCompany(conventionId: number): Observable<any> {
    const signRequest = {
      typeSignataire: 'ENTREPRISE'
    };
    return this.http.post(`${this.apiUrl}/conventions/${conventionId}/sign`, signRequest);
  }

  signConventionAsStudent(conventionId: number): Observable<any> {
    const signRequest = {
      typeSignataire: 'ETUDIANT'
    };
    return this.http.post(`${this.apiUrl}/conventions/${conventionId}/sign`, signRequest);
  }

  downloadConventionPdf(conventionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/conventions/${conventionId}/pdf`, { responseType: 'blob' });
  }
}