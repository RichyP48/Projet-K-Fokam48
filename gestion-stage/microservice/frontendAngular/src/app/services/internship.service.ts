import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

interface InternshipOffer {
  id?: number;
  title: string;
  domain: string;
  description: string;
  location: string;
  duration: string;
  startDate: string;
  salary: number;
  requiredSkills: string;
  companyId: number;
  companyName: string;
  status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED';
}
@Injectable({
  providedIn: 'root'
})
export class InternshipService {
  private baseUrl = 'http://localhost:8090/api';

  constructor(private http: HttpClient) {}

  // Offres
  getAllOffers(): Observable<any> {
    return this.http.get(`${this.baseUrl}/offers`);
  }

  getOfferById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/offers/${id}`);
  }

  // Candidatures
  createApplication(offerId: number, coverLetter: string, cvFile: File): Observable<any> {
    const formData = new FormData();
    formData.append('offreId', offerId.toString());
    formData.append('etudiantId', '2'); // TODO: récupérer depuis JWT
    formData.append('commentaires', coverLetter);
    formData.append('cv', cvFile);
    return this.http.post(`${this.baseUrl}/candidatures`, formData);
  }

  createOffer(offer: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/offers`, offer);
  }
   getCompanyOffers(page = 0, size = 10): Observable<InternshipOffer[]> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('companyId', '4'); // TODO: récupérer depuis JWT
    
    return this.http.get<InternshipOffer[]>(`${this.baseUrl}/offers`, { params });
  }

  getApplicationsByStudent(): Observable<any> {
    return this.http.get(`${this.baseUrl}/candidatures/etudiant/me`);
  }

  // Conventions
  getAgreementsByStudent(): Observable<any> {
    console.log('Appel API vers:', `${this.baseUrl}/conventions/etudiant/2`);
    return this.http.get(`${this.baseUrl}/conventions/etudiant/2`); // TODO: récupérer ID depuis JWT
  }

  signAgreement(agreementId: number, signature: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/conventions/${agreementId}/sign`, signature);
  }
  updateOffer(offer: InternshipOffer): Observable<InternshipOffer> {
    return this.http.put<InternshipOffer>(`${this.baseUrl}/offers/${offer.id}`, offer);
  }

  // Supprime une offre de stage par son ID
  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/offers/${id}`);
  }
}
