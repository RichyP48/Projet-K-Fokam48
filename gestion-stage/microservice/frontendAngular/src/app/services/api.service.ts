import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}
   get<T>(path: string, params: HttpParams = new HttpParams(), responseType?: string): Observable<T> {
    if (responseType === 'blob') {
      return this.http.get(`${this.apiUrl}${path}`, { 
        params, 
        responseType: 'blob',
        observe: 'body'
      }) as unknown as Observable<T>;
    }
    
    return this.http.get<T>(`${this.apiUrl}${path}`, { params });
  }

  post<T>(path: string, body: any = {}, responseType?: string): Observable<T> {
    if (responseType === 'blob') {
      return this.http.post(`${this.apiUrl}${path}`, body, {
        responseType: 'blob',
        observe: 'body'
      }) as unknown as Observable<T>;
    }
    
    return this.http.post<T>(`${this.apiUrl}${path}`, body);
  }

  put<T>(path: string, body: any = {}): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}${path}`, body);
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}${path}`);
  }

  getBlob(path: string, params: HttpParams = new HttpParams()): Observable<Blob> {
    return this.http.get(`${this.apiUrl}${path}`, { 
      params, 
      responseType: 'blob' 
    });
  }

  /**
   * Creates FormData from a mix of file and JSON data
   * @param files Object containing file fields and their File objects
   * @param jsonData Object containing non-file fields
   */
  createFormData(files: Record<string, File>, jsonData: Record<string, any> = {}): FormData {
    const formData = new FormData();
    
    // Add files to form data
    Object.keys(files).forEach(key => {
      if (files[key]) {
        formData.append(key, files[key], files[key].name);
      }
    });
    
    // Add JSON data to form data
    Object.keys(jsonData).forEach(key => {
      if (jsonData[key] !== undefined && jsonData[key] !== null) {
        formData.append(key, jsonData[key]);
      }
    });
    
    return formData;
  }





  
  // ===== AUTHENTICATION =====
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, credentials);
  }

  registerStudent(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register/student`, data);
  }

  registerCompany(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register/company`, data);
  }

  // ===== OFFERS =====
  getAllOffers(page = 0, size = 10, filters?: any): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (filters) {
      Object.keys(filters).forEach(key => {
        if (filters[key]) {
          params = params.set(key, filters[key]);
        }
      });
    }
    
    return this.http.get(`${this.apiUrl}/offers`, { params });
  }

  getOfferById(offerId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/offers/${offerId}`);
  }

  createOffer(offer: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/offers`, offer);
  }

  updateOffer(offerId: number, offer: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/offers/${offerId}`, offer);
  }

  updateOfferStatus(offerId: number, status: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/offers/${offerId}/status`, status);
  }

  deleteOffer(offerId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/offers/${offerId}`);
  }

  getCompanyOffers(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/companies/me/offers`, { params });
  }

  // ===== COMPANY AGREEMENTS =====
  getCompanyAgreements(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/companies/me/agreements`, { params });
  }

  signCompanyAgreement(agreementId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/companies/me/agreements/${agreementId}/sign`, {});
  }

  // ===== FACULTY AGREEMENTS =====
  getPendingAgreements(): Observable<any> {
    return this.http.get(`${this.apiUrl}/faculty/me/agreements/pending`);
  }

  validateAgreement(agreementId: number, payload: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/conventions/${agreementId}/validate`, payload);
  }

  // ===== ADMIN AGREEMENTS =====
  getPendingAgreementsAdmin(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/conventions/pending`, { params });
  }

  getAllAgreementsAdmin(page = 0, size = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/conventions`, { params });
  }

  approveAgreementAdmin(agreementId: number, payload: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/conventions/${agreementId}/approve`, payload);
  }

  downloadAgreementPdf(agreementId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/conventions/${agreementId}/pdf`, { responseType: 'blob' });
  }

  // ===== APPLICATIONS =====
  submitApplication(offerId: number, coverLetter: string, cvFile: File): Observable<any> {
    const formData = new FormData();
    formData.append('offreId', offerId.toString());
    formData.append('commentaires', coverLetter);
    formData.append('cv', cvFile);
    
    return this.http.post(`${this.apiUrl}/candidatures`, formData);
  }

  getStudentApplications(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/candidatures/etudiant/me`, { params });
  }

  // ===== STUDENT AGREEMENTS =====
  getStudentAgreements(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/students/me/agreements`, { params });
  }

  signStudentAgreement(agreementId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/students/me/agreements/${agreementId}/sign`, {});
  }

  getCompanyApplications(page = 0, size = 10, offerId?: number): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    // SECURITY FIX: Always use the secure company endpoint
    // The backend will handle offer filtering if needed
    if (offerId) {
      params = params.set('offerId', offerId.toString());
    }
    return this.http.get(`${this.apiUrl}/candidatures/entreprise/me`, { params });
    // return this.http.get(`${this.apiUrl}/candidatures/entreprise/me`, { params });
  }

  getApplicationById(applicationId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/candidatures/${applicationId}`);
  }

  updateApplicationStatus(applicationId: number, status: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/candidatures/${applicationId}/status`, status);
  }

  // ===== AGREEMENTS =====
  getAgreementById(agreementId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions/${agreementId}`);
  }

  // ===== COMPANIES =====
  getCurrentCompany(): Observable<any> {
    return this.http.get(`${this.apiUrl}/companies/me`);
  }

  updateCurrentCompany(data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/companies/me`, data);
  }

  // ===== ADMIN COMPANIES =====
  getAllCompaniesAdmin(page = 0, size = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${this.apiUrl}/admin/companies`, { params });
  }

  getCompanyByIdAdmin(companyId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/admin/companies/${companyId}`);
  }

  updateCompanyAdmin(companyId: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/companies/${companyId}`, data);
  }

  deleteCompanyAdmin(companyId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/companies/${companyId}`);
  }

  // ===== ADMIN REPORTS =====
  getAdminReports(period: string = 'month'): Observable<any> {
    const params = new HttpParams().set('periode', period);
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params });
  }

  exportAdminReport(period: string = 'month'): Observable<Blob> {
    const params = new HttpParams().set('type', 'stages_filiere').set('periode', period);
    return this.http.get(`${this.apiUrl}/reports/export/excel`, { 
      params, 
      responseType: 'blob' 
    });
  }

  getAdminSystemStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params: new HttpParams().set('periode', 'month') });
  }

  getAdminUserActivity(period: string = 'month'): Observable<any> {
    const params = new HttpParams().set('periode', period);
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params });
  }

  getAdminPlatformUsage(months: number = 6): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params });
  }

  getAdminTopCompanies(limit: number = 5): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params });
  }

  getAdminSystemHealth(): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params });
  }

  getAgreementStatsAdmin(): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/reports/stats/filiere`, { params });
  }

  rejectAgreementAdmin(agreementId: number, rejectionData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/agreements/${agreementId}/reject`, rejectionData);
  }

  // ===== CONVENTIONS =====
  generateConvention(request: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/conventions/generate`, request);
  }

  getConventionPdf(conventionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/conventions/${conventionId}/pdf`, { responseType: 'blob' });
  }

  validateConvention(conventionId: number, enseignantId: number): Observable<any> {
    const params = new HttpParams().set('enseignantId', enseignantId.toString());
    return this.http.put(`${this.apiUrl}/conventions/${conventionId}/validate`, {}, { params });
  }

  signConvention(conventionId: number, signRequest: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/conventions/${conventionId}/sign`, signRequest);
  }

  getStudentConventions(studentId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions/etudiant/${studentId}`);
  }

  getTeacherConventions(teacherId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/conventions/enseignant/${teacherId}`);
  }

  // ===== SUPPORTING RESOURCES =====
  getAllSkills(): Observable<any> {
    return this.http.get(`${this.apiUrl}/skills`);
  }

  getAllDomains(): Observable<any> {
    return this.http.get(`${this.apiUrl}/domains`);
  }

  getAllSectors(): Observable<any> {
    return this.http.get(`${this.apiUrl}/sectors`);
  }
}
