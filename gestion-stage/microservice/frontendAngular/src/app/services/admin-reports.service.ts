import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface StatistiqueFiliereDto {
  filiere: string;
  periode: string;
  nombreEtudiants: number;
  nombreStages: number;
  nombreCandidatures: number;
  tauxPlacement: number;
  nombreConventions: number;
  nombreConventionsSignees: number;
  tauxSignature: number;
}

export interface AdminReportData {
  systemStats: {
    totalUsers: number;
    totalStudents: number;
    totalCompanies: number;
    totalFaculty: number;
    totalOffers: number;
    totalApplications: number;
    totalAgreements: number;
  };
  userActivity: {
    dailyLogins: number;
    weeklyLogins: number;
    monthlyLogins: number;
  };
  platformUsage: {
    month: string;
    users: number;
    offers: number;
    applications: number;
  }[];
  topCompanies: {
    name: string;
    offers: number;
    applications: number;
    rating: number;
  }[];
  systemHealth: {
    uptime: number;
    responseTime: number;
    errorRate: number;
    storage: number;
  };
  agreementStats: {
    totalAgreements: number;
    pendingAgreements: number;
    approvedAgreements: number;
    rejectedAgreements: number;
    signedAgreements: number;
  };
  conventionStats?: {
    totalConventions: number;
    totalSignees: number;
    tauxSignatureMoyen: number;
    detailsParFiliere: any;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AdminReportsService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getReports(period: string = 'month'): Observable<StatistiqueFiliereDto[]> {
    const params = new HttpParams().set('periode', period);
    return this.http.get<StatistiqueFiliereDto[]>(`${this.apiUrl}/stats/filiere`, { params });
  }

  getConventionStats(period: string = 'month'): Observable<any> {
    const params = new HttpParams().set('periode', period);
    return this.http.get(`${this.apiUrl}/stats/conventions`, { params });
  }

  getSummaryStats(period: string = 'month'): Observable<any> {
    const params = new HttpParams().set('periode', period);
    return this.http.get(`${this.apiUrl}/stats/summary`, { params });
  }

  refreshStats(period: string = 'month'): Observable<any> {
    const params = new HttpParams().set('periode', period);
    return this.http.get(`${this.apiUrl}/stats/refresh`, { params });
  }

  getTopCompanies(limit: number = 5): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/admin/companies/top?limit=${limit}`);
  }

  exportReport(period: string = 'month'): Observable<Blob> {
    const params = new HttpParams().set('type', 'stages_filiere').set('periode', period);
    return this.http.get(`${this.apiUrl}/export/excel`, { 
      params, 
      responseType: 'blob' 
    });
  }

  getSystemStats(): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/stats/filiere`, { params });
  }

  getUserActivity(period: string = 'month'): Observable<any> {
    const params = new HttpParams().set('periode', period);
    return this.http.get(`${this.apiUrl}/stats/filiere`, { params });
  }

  getPlatformUsage(months: number = 6): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/stats/filiere`, { params });
  }

  getSystemHealth(): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/stats/filiere`, { params });
  }

  // ===== AGREEMENTS MANAGEMENT =====
  getAllAgreements(page = 0, size = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${environment.apiUrl}/conventions`, { params });
  }

  getPendingAgreements(page = 0, size = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get(`${environment.apiUrl}/conventions/pending`, { params });
  }

  getAgreementStats(): Observable<any> {
    const params = new HttpParams().set('periode', 'month');
    return this.http.get(`${this.apiUrl}/stats/filiere`, { params });
  }

  approveAgreement(agreementId: number, approvalData: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/conventions/${agreementId}/approve`, approvalData);
  }

  rejectAgreement(agreementId: number, rejectionData: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/conventions/${agreementId}/reject`, rejectionData);
  }
}
