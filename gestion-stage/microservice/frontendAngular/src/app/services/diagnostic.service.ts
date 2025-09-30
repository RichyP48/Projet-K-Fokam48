import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DiagnosticService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Test la connectivité avec l'API Gateway
   */
  testApiGateway(): Observable<{ status: string, message: string }> {
    return this.http.get(`${this.apiUrl}/actuator/health`).pipe(
      map(() => ({ status: 'OK', message: 'API Gateway accessible' })),
      catchError(error => of({ 
        status: 'ERROR', 
        message: `API Gateway inaccessible: ${error.status} ${error.statusText}` 
      }))
    );
  }

  /**
   * Test la connectivité avec le service utilisateur
   */
  testUserService(): Observable<{ status: string, message: string }> {
    return this.http.get(`${this.apiUrl}/users/health`).pipe(
      map(() => ({ status: 'OK', message: 'User Service accessible' })),
      catchError(error => of({ 
        status: 'ERROR', 
        message: `User Service inaccessible: ${error.status} ${error.statusText}` 
      }))
    );
  }

  /**
   * Test l'authentification
   */
  testAuth(): Observable<{ status: string, message: string, details?: any }> {
    const token = localStorage.getItem('auth_token');
    const userId = localStorage.getItem('user_id');
    const userRole = localStorage.getItem('user_role');

    if (!token) {
      return of({ status: 'ERROR', message: 'Aucun token trouvé' });
    }

    return this.http.get(`${this.apiUrl}/users/me`).pipe(
      map((user: any) => ({ 
        status: 'OK', 
        message: 'Authentification valide',
        details: { user, token: token.substring(0, 20) + '...', userId, userRole }
      })),
      catchError(error => of({ 
        status: 'ERROR', 
        message: `Erreur d'authentification: ${error.status} ${error.statusText}`,
        details: { token: token.substring(0, 20) + '...', userId, userRole }
      }))
    );
  }

  /**
   * Diagnostic complet
   */
  runFullDiagnostic(): Observable<any> {
    console.log('🔍 Démarrage du diagnostic complet...');
    
    const diagnostics = {
      apiGateway: this.testApiGateway(),
      userService: this.testUserService(),
      auth: this.testAuth()
    };

    return new Observable(observer => {
      const results: any = {};
      let completed = 0;
      const total = Object.keys(diagnostics).length;

      Object.entries(diagnostics).forEach(([key, test]) => {
        test.subscribe({
          next: (result) => {
            results[key] = result;
            completed++;
            console.log(`✅ ${key}:`, result);
            
            if (completed === total) {
              observer.next(results);
              observer.complete();
            }
          },
          error: (error) => {
            results[key] = { status: 'ERROR', message: error.message };
            completed++;
            console.error(`❌ ${key}:`, error);
            
            if (completed === total) {
              observer.next(results);
              observer.complete();
            }
          }
        });
      });
    });
  }
}
