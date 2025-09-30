export const environment = {
  production: false,
  apiUrl: 'http://localhost:8090/api'
};

// Log environment configuration
console.log('🔧 Environment loaded:', {
  production: environment.production,
  apiUrl: environment.apiUrl,
  timestamp: new Date().toISOString()
});
