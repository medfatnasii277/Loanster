# Borrower Service Testing - Issue Resolution

## Problem Identified

The main issues you're encountering are:

1. **@MockBean Deprecation**: `@MockBean` has been deprecated in newer Spring Boot versions
2. **Lombok Compilation Issues**: There are problems with Lombok annotation processing
3. **Missing Test Dependencies**: Some testing dependencies are missing

## Solutions

### 1. Fix @MockBean Issue

**Replace `@MockBean` with `@Mock` from Mockito:**

Instead of:
```java
@MockBean
private BorrowerService borrowerService;
```

Use:
```java
@Mock
private BorrowerService borrowerService;
```

### 2. Add Missing Dependencies to pom.xml

Add these dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Existing dependencies... -->
    
    <!-- Testing Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3. Fix Lombok Issues

Since Lombok seems to have compilation issues, here are two approaches:

#### Option A: Fix Lombok Configuration
1. Clean and rebuild the project:
```bash
mvn clean compile
```

2. Make sure your IDE has Lombok plugin installed
3. Ensure annotation processing is enabled

#### Option B: Use Manual Constructors (Recommended for testing)

Create test objects without builders:

```java
// Instead of using builders, create objects manually
BorrowerDto borrowerDto = new BorrowerDto();
borrowerDto.setId(1L);
borrowerDto.setFirstName("John");
borrowerDto.setLastName("Doe");
// ... set other fields
```

### 4. Corrected Controller Test Example

```java
@ExtendWith(MockitoExtension.class)
@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private BorrowerService borrowerService;

    @Mock
    private LoanApplicationService loanApplicationService;

    @Mock
    private LoanCalculatorService loanCalculatorService;

    @Mock
    private DocumentService documentService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public BorrowerService borrowerService() {
            return Mockito.mock(BorrowerService.class);
        }
        // ... other beans
    }

    @Test
    void createBorrower_ShouldReturnCreatedBorrower() throws Exception {
        // Create test data manually
        BorrowerDto borrowerDto = new BorrowerDto();
        borrowerDto.setId(1L);
        borrowerDto.setFirstName("John");
        borrowerDto.setLastName("Doe");
        
        CreateBorrowerRequest request = new CreateBorrowerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        
        when(borrowerService.createBorrower(any())).thenReturn(borrowerDto);

        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

### 5. Steps to Fix Your Project

1. **Update dependencies in pom.xml** (as shown above)

2. **Clean and rebuild**:
```bash
mvn clean compile
```

3. **Replace @MockBean with @Mock** in all test files

4. **Use @TestConfiguration** to provide mocked beans for @WebMvcTest

5. **Create test objects manually** instead of using builders if Lombok issues persist

### 6. Alternative: Simpler Integration Test

If the above doesn't work, create simpler integration tests:

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BorrowerServiceSimpleIntegrationTest {

    @Autowired
    private BorrowerService borrowerService;

    @Test
    void createBorrower_ShouldWork() {
        CreateBorrowerRequest request = new CreateBorrowerRequest();
        // Set required fields manually
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        // ... other required fields

        BorrowerDto result = borrowerService.createBorrower(request);
        
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }
}
```

## Quick Fix Command

Run this command to try to resolve compilation issues:

```bash
mvn clean compile -U
```

If Lombok issues persist, you can temporarily disable Lombok and create getters/setters manually for testing purposes.

## Summary

The main issue is the deprecated `@MockBean` annotation. Replace it with `@Mock` from Mockito and use `@TestConfiguration` to provide mocked beans for your @WebMvcTest classes. This should resolve the red error you're seeing.
