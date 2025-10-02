# Test Guide: Candidature Submission Issues

## Problems Fixed

### 1. UserService Hardcoded User ID
- **Issue**: UserService.getCurrentUserId() was hardcoded to return student ID 2
- **Fix**: Implemented proper JWT token parsing to extract user ID
- **Files**: `applications-service/src/main/java/com/mogou/service/UserService.java`

### 2. Missing JWT Dependencies
- **Issue**: JWT parsing libraries were missing
- **Fix**: Added `jjwt` and `spring-boot-starter-security` dependencies
- **Files**: `applications-service/pom.xml`

### 3. Missing Security Configuration
- **Issue**: No JWT authentication filter or security configuration
- **Fix**: Created SecurityConfig and JwtAuthenticationFilter
- **Files**: 
  - `applications-service/src/main/java/com/mogou/config/SecurityConfig.java`
  - `applications-service/src/main/java/com/mogou/config/JwtAuthenticationFilter.java`

### 4. CORS Issues
- **Issue**: Frontend requests blocked by CORS policy
- **Fix**: Added CORS configuration to both API Gateway and applications-service
- **Files**: 
  - `api-gateway/src/main/java/com/mogou/config/CorsConfig.java`
  - `applications-service/src/main/java/com/mogou/config/CorsConfig.java`

### 5. Poor Error Handling
- **Issue**: Generic error messages not helpful for debugging
- **Fix**: Enhanced error handling with specific validation and error messages
- **Files**: 
  - `applications-service/src/main/java/com/mogou/controller/CandidatureController.java`
  - `frontendAngular/src/app/components/offers/pages/offer-apply/offer-apply.component.ts`

### 6. File Upload Issues
- **Issue**: MinIO service might not be available or configured properly
- **Fix**: Enhanced FileStorageService with better error handling and logging
- **Files**: `applications-service/src/main/java/com/mogou/service/FileStorageService.java`

## Testing Steps

### 1. Check Service Health
```bash
curl http://localhost:8082/api/candidatures/health
```

### 2. Test Basic Connectivity
```bash
curl http://localhost:8082/api/candidatures/test
```

### 3. Test Submission Parameters
```bash
curl -X POST http://localhost:8082/api/candidatures/test-submit \
  -F "offreId=1" \
  -F "commentaires=Test comment" \
  -F "cv=@test.pdf"
```

### 4. Check MinIO Service
```bash
docker-compose ps minio
```

### 5. Verify Database Connection
```bash
docker-compose ps postgres
```

## Common Issues and Solutions

### "Erreur lors de l'envoi" Message
1. **Check JWT Token**: Ensure user is properly logged in
2. **Verify MinIO**: Ensure MinIO service is running
3. **Check Database**: Ensure PostgreSQL is accessible
4. **File Size**: Ensure CV file is under 5MB
5. **File Type**: Ensure CV is PDF, DOC, or DOCX

### Service Not Responding
1. **Check Eureka**: Verify service is registered
2. **Check Ports**: Ensure port 8082 is not blocked
3. **Check Logs**: Review application logs for errors

### File Upload Fails
1. **MinIO Status**: Check if MinIO container is running
2. **Bucket Creation**: Verify 'candidatures' bucket exists
3. **Permissions**: Check MinIO access credentials

## Next Steps
1. Restart applications-service after changes
2. Test with a real student login
3. Monitor logs during submission
4. Verify file appears in MinIO console