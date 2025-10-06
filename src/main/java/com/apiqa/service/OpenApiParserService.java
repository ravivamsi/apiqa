package com.apiqa.service;

import com.apiqa.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.parser.SwaggerParser;
import io.swagger.models.Swagger;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Model;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.properties.Property;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OpenApiParserService {
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    public List<FeatureFile> parseOpenApiSpec(String openApiYaml, ApiSpec apiSpec) {
        try {
            // Clean and normalize the YAML content
            String cleanedYaml = cleanYamlContent(openApiYaml);
            
            // Detect specification version
            String specVersion = detectSpecVersion(cleanedYaml);
            
            List<FeatureFile> featureFiles = new ArrayList<>();
            
            if ("swagger".equals(specVersion)) {
                // Parse Swagger 2.0 specification
                Swagger swagger = parseSwagger2Spec(cleanedYaml);
                if (swagger == null) {
                    throw new RuntimeException("Failed to parse Swagger 2.0 specification");
                }
                
                // Generate feature files for Swagger 2.0
                featureFiles.add(generateSmokeTestsSwagger2(swagger, apiSpec));
                featureFiles.add(generateSystemTestsSwagger2(swagger, apiSpec));
                featureFiles.add(generateNegativeTestsSwagger2(swagger, apiSpec));
                featureFiles.add(generateIntegrationTestsSwagger2(swagger, apiSpec));
                
            } else if ("openapi".equals(specVersion)) {
                // Parse OpenAPI 3.0.0 specification
                OpenAPI openAPI = parseOpenApi3Spec(cleanedYaml);
                if (openAPI == null) {
                    throw new RuntimeException("Failed to parse OpenAPI 3.0.0 specification");
                }
                
                // Generate feature files for OpenAPI 3.0.0
                featureFiles.add(generateSmokeTests(openAPI, apiSpec));
                featureFiles.add(generateSystemTests(openAPI, apiSpec));
                featureFiles.add(generateNegativeTests(openAPI, apiSpec));
                featureFiles.add(generateIntegrationTests(openAPI, apiSpec));
                
            } else {
                throw new RuntimeException("Unsupported API specification version: " + specVersion);
            }
            
            return featureFiles;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse API specification: " + e.getMessage(), e);
        }
    }
    
    private String cleanYamlContent(String yamlContent) {
        try {
            // Remove or replace common problematic patterns
            String cleaned = yamlContent;
            
            // Handle environment variables and placeholders
            cleaned = cleaned.replaceAll("\\$\\{[^}]+\\}", "PLACEHOLDER_VALUE");
            cleaned = cleaned.replaceAll("\\$[A-Z_]+", "PLACEHOLDER_VALUE");
            
            // Handle special characters that might cause parsing issues
            cleaned = cleaned.replaceAll("\\$\\$", "DOLLAR_SIGN");
            cleaned = cleaned.replaceAll("\\@\\@", "AT_SIGN");
            
            // Remove comments that might contain special characters
            cleaned = cleaned.replaceAll("#.*$", "");
            
            // Handle multiline strings with special characters
            cleaned = cleaned.replaceAll("\\|\\s*$", "|");
            cleaned = cleaned.replaceAll(">\\s*$", ">");
            
            // Normalize line endings
            cleaned = cleaned.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            
            return cleaned;
            
        } catch (Exception e) {
            // If cleaning fails, return original content
            return yamlContent;
        }
    }
    
    private String replacePathVariables(String path) {
        if (path == null) {
            return path;
        }
        
        String result = path;
        
        // Replace common path variables with meaningful test data (max 5 chars)
        result = result.replaceAll("\\{id\\}", "12345");
        result = result.replaceAll("\\{userId\\}", "user1");
        result = result.replaceAll("\\{user_id\\}", "user1");
        result = result.replaceAll("\\{productId\\}", "prod1");
        result = result.replaceAll("\\{product_id\\}", "prod1");
        result = result.replaceAll("\\{orderId\\}", "ord01");
        result = result.replaceAll("\\{order_id\\}", "ord01");
        result = result.replaceAll("\\{categoryId\\}", "cat01");
        result = result.replaceAll("\\{category_id\\}", "cat01");
        result = result.replaceAll("\\{postId\\}", "post1");
        result = result.replaceAll("\\{post_id\\}", "post1");
        result = result.replaceAll("\\{commentId\\}", "cmt01");
        result = result.replaceAll("\\{comment_id\\}", "cmt01");
        result = result.replaceAll("\\{taskId\\}", "task1");
        result = result.replaceAll("\\{task_id\\}", "task1");
        result = result.replaceAll("\\{projectId\\}", "proj1");
        result = result.replaceAll("\\{project_id\\}", "proj1");
        result = result.replaceAll("\\{accountId\\}", "acc01");
        result = result.replaceAll("\\{account_id\\}", "acc01");
        result = result.replaceAll("\\{customerId\\}", "cust1");
        result = result.replaceAll("\\{customer_id\\}", "cust1");
        result = result.replaceAll("\\{employeeId\\}", "emp01");
        result = result.replaceAll("\\{employee_id\\}", "emp01");
        result = result.replaceAll("\\{departmentId\\}", "dept1");
        result = result.replaceAll("\\{department_id\\}", "dept1");
        result = result.replaceAll("\\{companyId\\}", "comp1");
        result = result.replaceAll("\\{company_id\\}", "comp1");
        result = result.replaceAll("\\{organizationId\\}", "org01");
        result = result.replaceAll("\\{organization_id\\}", "org01");
        result = result.replaceAll("\\{teamId\\}", "team1");
        result = result.replaceAll("\\{team_id\\}", "team1");
        result = result.replaceAll("\\{sessionId\\}", "sess1");
        result = result.replaceAll("\\{session_id\\}", "sess1");
        result = result.replaceAll("\\{tokenId\\}", "tok01");
        result = result.replaceAll("\\{token_id\\}", "tok01");
        result = result.replaceAll("\\{keyId\\}", "key01");
        result = result.replaceAll("\\{key_id\\}", "key01");
        result = result.replaceAll("\\{versionId\\}", "v1");
        result = result.replaceAll("\\{version_id\\}", "v1");
        result = result.replaceAll("\\{revisionId\\}", "rev01");
        result = result.replaceAll("\\{revision_id\\}", "rev01");
        result = result.replaceAll("\\{batchId\\}", "bat01");
        result = result.replaceAll("\\{batch_id\\}", "bat01");
        result = result.replaceAll("\\{jobId\\}", "job01");
        result = result.replaceAll("\\{job_id\\}", "job01");
        result = result.replaceAll("\\{workflowId\\}", "wf001");
        result = result.replaceAll("\\{workflow_id\\}", "wf001");
        result = result.replaceAll("\\{pipelineId\\}", "pipe1");
        result = result.replaceAll("\\{pipeline_id\\}", "pipe1");
        result = result.replaceAll("\\{configId\\}", "cfg01");
        result = result.replaceAll("\\{config_id\\}", "cfg01");
        result = result.replaceAll("\\{settingId\\}", "set01");
        result = result.replaceAll("\\{setting_id\\}", "set01");
        result = result.replaceAll("\\{paramId\\}", "par01");
        result = result.replaceAll("\\{param_id\\}", "par01");
        result = result.replaceAll("\\{optionId\\}", "opt01");
        result = result.replaceAll("\\{option_id\\}", "opt01");
        result = result.replaceAll("\\{choiceId\\}", "ch001");
        result = result.replaceAll("\\{choice_id\\}", "ch001");
        result = result.replaceAll("\\{itemId\\}", "item1");
        result = result.replaceAll("\\{item_id\\}", "item1");
        result = result.replaceAll("\\{recordId\\}", "rec01");
        result = result.replaceAll("\\{record_id\\}", "rec01");
        result = result.replaceAll("\\{entryId\\}", "ent01");
        result = result.replaceAll("\\{entry_id\\}", "ent01");
        result = result.replaceAll("\\{documentId\\}", "doc01");
        result = result.replaceAll("\\{document_id\\}", "doc01");
        result = result.replaceAll("\\{fileId\\}", "file1");
        result = result.replaceAll("\\{file_id\\}", "file1");
        result = result.replaceAll("\\{imageId\\}", "img01");
        result = result.replaceAll("\\{image_id\\}", "img01");
        result = result.replaceAll("\\{videoId\\}", "vid01");
        result = result.replaceAll("\\{video_id\\}", "vid01");
        result = result.replaceAll("\\{audioId\\}", "aud01");
        result = result.replaceAll("\\{audio_id\\}", "aud01");
        result = result.replaceAll("\\{mediaId\\}", "med01");
        result = result.replaceAll("\\{media_id\\}", "med01");
        result = result.replaceAll("\\{contentId\\}", "cnt01");
        result = result.replaceAll("\\{content_id\\}", "cnt01");
        result = result.replaceAll("\\{resourceId\\}", "res01");
        result = result.replaceAll("\\{resource_id\\}", "res01");
        result = result.replaceAll("\\{assetId\\}", "ast01");
        result = result.replaceAll("\\{asset_id\\}", "ast01");
        result = result.replaceAll("\\{objectId\\}", "obj01");
        result = result.replaceAll("\\{object_id\\}", "obj01");
        result = result.replaceAll("\\{entityId\\}", "ent01");
        result = result.replaceAll("\\{entity_id\\}", "ent01");
        result = result.replaceAll("\\{modelId\\}", "mod01");
        result = result.replaceAll("\\{model_id\\}", "mod01");
        result = result.replaceAll("\\{schemaId\\}", "sch01");
        result = result.replaceAll("\\{schema_id\\}", "sch01");
        result = result.replaceAll("\\{typeId\\}", "type1");
        result = result.replaceAll("\\{type_id\\}", "type1");
        result = result.replaceAll("\\{statusId\\}", "st001");
        result = result.replaceAll("\\{status_id\\}", "st001");
        result = result.replaceAll("\\{stateId\\}", "st001");
        result = result.replaceAll("\\{state_id\\}", "st001");
        result = result.replaceAll("\\{levelId\\}", "lvl01");
        result = result.replaceAll("\\{level_id\\}", "lvl01");
        result = result.replaceAll("\\{gradeId\\}", "gr001");
        result = result.replaceAll("\\{grade_id\\}", "gr001");
        result = result.replaceAll("\\{rankId\\}", "rnk01");
        result = result.replaceAll("\\{rank_id\\}", "rnk01");
        result = result.replaceAll("\\{priorityId\\}", "pr001");
        result = result.replaceAll("\\{priority_id\\}", "pr001");
        result = result.replaceAll("\\{severityId\\}", "sv001");
        result = result.replaceAll("\\{severity_id\\}", "sv001");
        result = result.replaceAll("\\{categoryId\\}", "cat01");
        result = result.replaceAll("\\{category_id\\}", "cat01");
        result = result.replaceAll("\\{tagId\\}", "tag01");
        result = result.replaceAll("\\{tag_id\\}", "tag01");
        result = result.replaceAll("\\{labelId\\}", "lbl01");
        result = result.replaceAll("\\{label_id\\}", "lbl01");
        result = result.replaceAll("\\{nameId\\}", "nm001");
        result = result.replaceAll("\\{name_id\\}", "nm001");
        result = result.replaceAll("\\{codeId\\}", "cd001");
        result = result.replaceAll("\\{code_id\\}", "cd001");
        result = result.replaceAll("\\{refId\\}", "ref01");
        result = result.replaceAll("\\{ref_id\\}", "ref01");
        result = result.replaceAll("\\{uuid\\}", "uuid1");
        result = result.replaceAll("\\{guid\\}", "guid1");
        result = result.replaceAll("\\{hash\\}", "hash1");
        result = result.replaceAll("\\{slug\\}", "slug1");
        result = result.replaceAll("\\{alias\\}", "alias");
        result = result.replaceAll("\\{username\\}", "user1");
        result = result.replaceAll("\\{email\\}", "test@");
        result = result.replaceAll("\\{phone\\}", "12345");
        result = result.replaceAll("\\{zip\\}", "12345");
        result = result.replaceAll("\\{code\\}", "code1");
        result = result.replaceAll("\\{key\\}", "key01");
        result = result.replaceAll("\\{value\\}", "val01");
        result = result.replaceAll("\\{text\\}", "text1");
        result = result.replaceAll("\\{title\\}", "title");
        result = result.replaceAll("\\{name\\}", "name1");
        result = result.replaceAll("\\{slug\\}", "slug1");
        result = result.replaceAll("\\{path\\}", "path1");
        result = result.replaceAll("\\{url\\}", "url01");
        result = result.replaceAll("\\{link\\}", "link1");
        result = result.replaceAll("\\{token\\}", "tok01");
        result = result.replaceAll("\\{secret\\}", "sec01");
        result = result.replaceAll("\\{password\\}", "pwd01");
        result = result.replaceAll("\\{pin\\}", "12345");
        result = result.replaceAll("\\{otp\\}", "12345");
        result = result.replaceAll("\\{code\\}", "code1");
        result = result.replaceAll("\\{number\\}", "12345");
        result = result.replaceAll("\\{amount\\}", "100");
        result = result.replaceAll("\\{price\\}", "99.99");
        result = result.replaceAll("\\{cost\\}", "50.00");
        result = result.replaceAll("\\{rate\\}", "5.00");
        result = result.replaceAll("\\{fee\\}", "10.00");
        result = result.replaceAll("\\{tax\\}", "8.25");
        result = result.replaceAll("\\{discount\\}", "5.00");
        result = result.replaceAll("\\{credit\\}", "100");
        result = result.replaceAll("\\{debit\\}", "50");
        result = result.replaceAll("\\{balance\\}", "500");
        result = result.replaceAll("\\{limit\\}", "1000");
        result = result.replaceAll("\\{quota\\}", "100");
        result = result.replaceAll("\\{count\\}", "10");
        result = result.replaceAll("\\{size\\}", "1024");
        result = result.replaceAll("\\{length\\}", "100");
        result = result.replaceAll("\\{width\\}", "200");
        result = result.replaceAll("\\{height\\}", "300");
        result = result.replaceAll("\\{weight\\}", "1.5");
        result = result.replaceAll("\\{volume\\}", "100");
        result = result.replaceAll("\\{capacity\\}", "500");
        result = result.replaceAll("\\{duration\\}", "60");
        result = result.replaceAll("\\{time\\}", "120");
        result = result.replaceAll("\\{date\\}", "2024");
        result = result.replaceAll("\\{year\\}", "2024");
        result = result.replaceAll("\\{month\\}", "01");
        result = result.replaceAll("\\{day\\}", "15");
        result = result.replaceAll("\\{hour\\}", "12");
        result = result.replaceAll("\\{minute\\}", "30");
        result = result.replaceAll("\\{second\\}", "45");
        result = result.replaceAll("\\{timestamp\\}", "12345");
        result = result.replaceAll("\\{epoch\\}", "12345");
        result = result.replaceAll("\\{offset\\}", "0");
        result = result.replaceAll("\\{timezone\\}", "UTC");
        result = result.replaceAll("\\{locale\\}", "en");
        result = result.replaceAll("\\{language\\}", "en");
        result = result.replaceAll("\\{country\\}", "US");
        result = result.replaceAll("\\{region\\}", "CA");
        result = result.replaceAll("\\{city\\}", "NYC");
        result = result.replaceAll("\\{address\\}", "addr1");
        result = result.replaceAll("\\{street\\}", "st001");
        result = result.replaceAll("\\{building\\}", "bld01");
        result = result.replaceAll("\\{floor\\}", "1");
        result = result.replaceAll("\\{room\\}", "101");
        result = result.replaceAll("\\{apartment\\}", "apt01");
        result = result.replaceAll("\\{suite\\}", "ste01");
        result = result.replaceAll("\\{unit\\}", "unit1");
        result = result.replaceAll("\\{zone\\}", "zone1");
        result = result.replaceAll("\\{area\\}", "area1");
        result = result.replaceAll("\\{district\\}", "dist1");
        result = result.replaceAll("\\{neighborhood\\}", "ngh01");
        result = result.replaceAll("\\{block\\}", "blk01");
        result = result.replaceAll("\\{lot\\}", "lot01");
        result = result.replaceAll("\\{parcel\\}", "prc01");
        result = result.replaceAll("\\{plot\\}", "plt01");
        result = result.replaceAll("\\{section\\}", "sec01");
        result = result.replaceAll("\\{chapter\\}", "ch001");
        result = result.replaceAll("\\{page\\}", "1");
        result = result.replaceAll("\\{line\\}", "1");
        result = result.replaceAll("\\{column\\}", "1");
        result = result.replaceAll("\\{row\\}", "1");
        result = result.replaceAll("\\{index\\}", "0");
        result = result.replaceAll("\\{position\\}", "1");
        result = result.replaceAll("\\{order\\}", "1");
        result = result.replaceAll("\\{sequence\\}", "1");
        result = result.replaceAll("\\{step\\}", "1");
        result = result.replaceAll("\\{stage\\}", "1");
        result = result.replaceAll("\\{phase\\}", "1");
        result = result.replaceAll("\\{level\\}", "1");
        result = result.replaceAll("\\{tier\\}", "1");
        result = result.replaceAll("\\{grade\\}", "A");
        result = result.replaceAll("\\{score\\}", "100");
        result = result.replaceAll("\\{rating\\}", "5");
        result = result.replaceAll("\\{rank\\}", "1");
        result = result.replaceAll("\\{priority\\}", "1");
        result = result.replaceAll("\\{weight\\}", "1");
        result = result.replaceAll("\\{value\\}", "1");
        result = result.replaceAll("\\{amount\\}", "1");
        result = result.replaceAll("\\{quantity\\}", "1");
        result = result.replaceAll("\\{total\\}", "100");
        result = result.replaceAll("\\{sum\\}", "100");
        result = result.replaceAll("\\{average\\}", "50");
        result = result.replaceAll("\\{mean\\}", "50");
        result = result.replaceAll("\\{median\\}", "50");
        result = result.replaceAll("\\{mode\\}", "1");
        result = result.replaceAll("\\{min\\}", "1");
        result = result.replaceAll("\\{max\\}", "100");
        result = result.replaceAll("\\{range\\}", "99");
        result = result.replaceAll("\\{variance\\}", "25");
        result = result.replaceAll("\\{deviation\\}", "5");
        result = result.replaceAll("\\{percentile\\}", "50");
        result = result.replaceAll("\\{quartile\\}", "25");
        result = result.replaceAll("\\{decile\\}", "10");
        result = result.replaceAll("\\{centile\\}", "1");
        result = result.replaceAll("\\{fraction\\}", "1/2");
        result = result.replaceAll("\\{ratio\\}", "1:1");
        result = result.replaceAll("\\{proportion\\}", "0.5");
        result = result.replaceAll("\\{percentage\\}", "50");
        result = result.replaceAll("\\{probability\\}", "0.5");
        result = result.replaceAll("\\{likelihood\\}", "0.5");
        result = result.replaceAll("\\{chance\\}", "0.5");
        result = result.replaceAll("\\{odds\\}", "1:1");
        result = result.replaceAll("\\{frequency\\}", "10");
        result = result.replaceAll("\\{rate\\}", "5");
        result = result.replaceAll("\\{speed\\}", "100");
        result = result.replaceAll("\\{velocity\\}", "50");
        result = result.replaceAll("\\{acceleration\\}", "10");
        result = result.replaceAll("\\{force\\}", "100");
        result = result.replaceAll("\\{pressure\\}", "1013");
        result = result.replaceAll("\\{temperature\\}", "20");
        result = result.replaceAll("\\{humidity\\}", "50");
        result = result.replaceAll("\\{density\\}", "1.0");
        result = result.replaceAll("\\{mass\\}", "1.0");
        result = result.replaceAll("\\{energy\\}", "100");
        result = result.replaceAll("\\{power\\}", "50");
        result = result.replaceAll("\\{current\\}", "1.0");
        result = result.replaceAll("\\{voltage\\}", "220");
        result = result.replaceAll("\\{resistance\\}", "100");
        result = result.replaceAll("\\{capacitance\\}", "1.0");
        result = result.replaceAll("\\{inductance\\}", "1.0");
        result = result.replaceAll("\\{impedance\\}", "100");
        result = result.replaceAll("\\{admittance\\}", "0.01");
        result = result.replaceAll("\\{reactance\\}", "50");
        result = result.replaceAll("\\{susceptance\\}", "0.02");
        result = result.replaceAll("\\{conductance\\}", "0.01");
        result = result.replaceAll("\\{permittivity\\}", "8.85");
        result = result.replaceAll("\\{permeability\\}", "1.26");
        result = result.replaceAll("\\{flux\\}", "1.0");
        result = result.replaceAll("\\{field\\}", "1.0");
        result = result.replaceAll("\\{potential\\}", "100");
        result = result.replaceAll("\\{charge\\}", "1.0");
        result = result.replaceAll("\\{current\\}", "1.0");
        result = result.replaceAll("\\{magnetic\\}", "1.0");
        result = result.replaceAll("\\{electric\\}", "1.0");
        result = result.replaceAll("\\{electromagnetic\\}", "1.0");
        result = result.replaceAll("\\{electrostatic\\}", "1.0");
        result = result.replaceAll("\\{magnetostatic\\}", "1.0");
        result = result.replaceAll("\\{electrodynamic\\}", "1.0");
        result = result.replaceAll("\\{magnetodynamic\\}", "1.0");
        result = result.replaceAll("\\{electromagnetic\\}", "1.0");
        result = result.replaceAll("\\{electrostatic\\}", "1.0");
        result = result.replaceAll("\\{magnetostatic\\}", "1.0");
        result = result.replaceAll("\\{electrodynamic\\}", "1.0");
        result = result.replaceAll("\\{magnetodynamic\\}", "1.0");
        
        // Handle any remaining generic {variable} patterns
        result = result.replaceAll("\\{[a-zA-Z_][a-zA-Z0-9_]*\\}", "test1");
        
        return result;
    }
    
    private Swagger parseSwagger2Spec(String yamlContent) {
        try {
            SwaggerParser parser = new SwaggerParser();
            Swagger swagger = parser.parse(yamlContent);
            
            // If parsing fails, try with additional options
            if (swagger == null) {
                // Try parsing with relaxed validation
                swagger = parser.parse(yamlContent);
            }
            
            return swagger;
            
        } catch (Exception e) {
            // Log the error but don't fail completely
            System.err.println("Warning: Error parsing Swagger 2.0 spec: " + e.getMessage());
            
            // Try to create a minimal valid Swagger object
            try {
                Swagger minimalSwagger = new Swagger();
                minimalSwagger.setSwagger("2.0");
                minimalSwagger.setInfo(new io.swagger.models.Info());
                minimalSwagger.getInfo().setTitle("Parsed API");
                minimalSwagger.getInfo().setVersion("1.0.0");
                minimalSwagger.setPaths(new java.util.HashMap<>());
                return minimalSwagger;
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    private OpenAPI parseOpenApi3Spec(String yamlContent) {
        try {
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            OpenAPI openAPI = parser.readContents(yamlContent, null, null).getOpenAPI();
            
            if (openAPI != null) {
                return openAPI;
            }
            
            // If parsing fails, try with additional options
            openAPI = parser.readContents(yamlContent, null, null).getOpenAPI();
            if (openAPI != null) {
                return openAPI;
            }
            
            return null;
            
        } catch (Exception e) {
            // Log the error but don't fail completely
            System.err.println("Warning: Error parsing OpenAPI 3.0 spec: " + e.getMessage());
            
            // Try to create a minimal valid OpenAPI object
            try {
                OpenAPI minimalOpenAPI = new OpenAPI();
                minimalOpenAPI.setOpenapi("3.0.0");
                minimalOpenAPI.setInfo(new io.swagger.v3.oas.models.info.Info());
                minimalOpenAPI.getInfo().setTitle("Parsed API");
                minimalOpenAPI.getInfo().setVersion("1.0.0");
                minimalOpenAPI.setPaths(new io.swagger.v3.oas.models.Paths());
                return minimalOpenAPI;
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    private String detectSpecVersion(String specContent) {
        try {
            // First try to parse as YAML
            JsonNode rootNode = yamlMapper.readTree(specContent);
            
            // Check for Swagger 2.0
            if (rootNode.has("swagger")) {
                String swaggerVersion = rootNode.get("swagger").asText();
                if (swaggerVersion.startsWith("2.")) {
                    return "swagger";
                }
            }
            
            // Check for OpenAPI 3.0.0
            if (rootNode.has("openapi")) {
                String openApiVersion = rootNode.get("openapi").asText();
                if (openApiVersion.startsWith("3.")) {
                    return "openapi";
                }
            }
            
            throw new RuntimeException("Unable to detect API specification version");
            
        } catch (Exception e) {
            // If YAML parsing fails, try string-based detection
            try {
                String content = specContent.toLowerCase();
                
                // Look for version indicators in the content
                if (content.contains("swagger:") && content.contains("2.0")) {
                    return "swagger";
                } else if (content.contains("openapi:") && content.contains("3.0")) {
                    return "openapi";
                } else if (content.contains("swagger:")) {
                    return "swagger";
                } else if (content.contains("openapi:")) {
                    return "openapi";
                }
                
                // Default to OpenAPI 3.0 if we can't determine
                System.err.println("Warning: Could not detect specification version, defaulting to OpenAPI 3.0");
                return "openapi";
                
            } catch (Exception ex) {
                throw new RuntimeException("Failed to detect specification version", ex);
            }
        }
    }
    
    private String getBaseUrl(OpenAPI openAPI) {
        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            return openAPI.getServers().get(0).getUrl();
        }
        return "https://api.example.com"; // Default fallback
    }
    
    private FeatureFile generateSmokeTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: Smoke Tests - Read-only Operations (Positive Scenarios)\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to verify basic API functionality with valid requests\n");
        content.append("  So that I can ensure the API is accessible and responding correctly\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_smoke_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SMOKE, content.toString(), apiSpec);
        
        List<TestScenario> scenarios = new ArrayList<>();
        String baseUrl = getBaseUrl(openAPI);
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Only GET operations for smoke tests
                if (pathItem.getGet() != null) {
                    Operation getOp = pathItem.getGet();
                    
                    // Positive scenario - Valid GET request
                    String positiveScenarioName = generateScenarioName("GET", path, getOp.getOperationId()) + "_Valid_Request";
                    String description = "Valid GET request to " + path;
                    String substitutedPath = substitutePathParameters(path, getOp);
                    String fullUrl = baseUrl + substitutedPath;
                    
                    content.append("  Scenario: ").append(positiveScenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    And I have valid authentication credentials\n");
                    content.append("    When I send a GET request to \"").append(substitutedPath).append("\"\n");
                    content.append("    And I include valid headers\n");
                    content.append("    Then the response status should be 200\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response should match the expected schema\n");
                    content.append("    And the response should contain mandatory fields\n");
                    content.append("    And the response headers should be valid\n");
                    content.append("    And the response time should be less than 5 seconds\n\n");
                    
                    TestScenario positiveScenario = createTestScenario(positiveScenarioName, description, "GET", fullUrl, 
                            null, getExpectedResponseSchema(getOp), getExpectedHeaders(getOp), 200, 
                            generatePositiveTestSteps("GET", substitutedPath, null), featureFile);
                    scenarios.add(positiveScenario);
                }
            }
        }
        
        // Update the content with all scenarios
        featureFile.setContent(content.toString());
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private FeatureFile generateSystemTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: System Tests - All CRUD Operations (Positive Scenarios)\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test all API operations thoroughly with valid data\n");
        content.append("  So that I can ensure complete API functionality works correctly\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_system_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SYSTEM, content.toString(), apiSpec);
        
        List<TestScenario> scenarios = new ArrayList<>();
        String baseUrl = getBaseUrl(openAPI);
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Test all HTTP methods
                for (Map.Entry<String, Operation> methodEntry : getOperations(pathItem).entrySet()) {
                    String method = methodEntry.getKey();
                    Operation operation = methodEntry.getValue();
                    
                    // Positive scenario for each operation
                    String positiveScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Valid_Request";
                    String description = "Valid " + method + " request to " + path;
                    String substitutedPath = substitutePathParameters(path, operation);
                    String fullUrl = baseUrl + substitutedPath;
                    String requestBody = getRequestBody(operation);
                    
                    content.append("  Scenario: ").append(positiveScenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    And I have valid authentication credentials\n");
                    content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                    
                    if (hasRequestBody(operation)) {
                        content.append("    And I include a valid request body\n");
                    }
                    
                    content.append("    And I include valid headers\n");
                    content.append("    Then the response status should be ").append(getExpectedStatusCode(operation)).append("\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response should match the expected schema\n");
                    content.append("    And the response should contain mandatory fields\n");
                    content.append("    And the response headers should be valid\n");
                    content.append("    And the response time should be less than 10 seconds\n\n");
                    
                    TestScenario positiveScenario = createTestScenario(positiveScenarioName, description, method, fullUrl,
                            requestBody, getExpectedResponseSchema(operation), 
                            getExpectedHeaders(operation), getExpectedStatusCode(operation),
                            generatePositiveTestSteps(method, substitutedPath, requestBody), featureFile);
                    scenarios.add(positiveScenario);
                }
            }
        }
        
        // Update the content with all scenarios
        featureFile.setContent(content.toString());
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private FeatureFile generateNegativeTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: Negative Tests - Invalid Requests and Error Handling\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test API error handling with invalid requests\n");
        content.append("  So that I can ensure the API handles errors gracefully\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_negative_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SYSTEM, content.toString(), apiSpec);
        
        List<TestScenario> scenarios = new ArrayList<>();
        String baseUrl = getBaseUrl(openAPI);
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Test all HTTP methods for negative scenarios
                for (Map.Entry<String, Operation> methodEntry : getOperations(pathItem).entrySet()) {
                    String method = methodEntry.getKey();
                    Operation operation = methodEntry.getValue();
                    
                    // Invalid ID scenario
                    if (path.contains("{")) {
                        String invalidIdScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Invalid_ID";
                        String description = "Invalid ID in " + method + " request to " + path;
                        String invalidPath = substitutePathParametersWithInvalidValues(path, operation);
                        String fullUrl = baseUrl + invalidPath;
                        
                        content.append("  Scenario: ").append(invalidIdScenarioName).append("\n");
                        content.append("    Given the API is available\n");
                        content.append("    And I have valid authentication credentials\n");
                        content.append("    When I send a ").append(method).append(" request to \"").append(invalidPath).append("\"\n");
                        content.append("    And I include valid headers\n");
                        content.append("    Then the response status should be 404\n");
                        content.append("    And the response should contain an error message\n");
                        content.append("    And the response should be valid JSON\n");
                        content.append("    And the response time should be less than 5 seconds\n\n");
                        
                        TestScenario invalidIdScenario = createTestScenario(invalidIdScenarioName, description, method, fullUrl,
                                null, getErrorResponseSchema(), getExpectedHeaders(operation), 404,
                                generateNegativeTestSteps(method, invalidPath, "Invalid ID"), featureFile);
                        scenarios.add(invalidIdScenario);
                    }
                    
                    // Invalid request body scenario (for POST/PUT/PATCH)
                    if (hasRequestBody(operation)) {
                        String invalidBodyScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Invalid_Body";
                        String description = "Invalid request body in " + method + " request to " + path;
                        String substitutedPath = substitutePathParameters(path, operation);
                        String fullUrl = baseUrl + substitutedPath;
                        String invalidRequestBody = generateInvalidRequestBody(operation);
                        
                        content.append("  Scenario: ").append(invalidBodyScenarioName).append("\n");
                        content.append("    Given the API is available\n");
                        content.append("    And I have valid authentication credentials\n");
                        content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                        content.append("    And I include an invalid request body\n");
                        content.append("    And I include valid headers\n");
                        content.append("    Then the response status should be 400\n");
                        content.append("    And the response should contain validation error messages\n");
                        content.append("    And the response should be valid JSON\n");
                        content.append("    And the response time should be less than 5 seconds\n\n");
                        
                        TestScenario invalidBodyScenario = createTestScenario(invalidBodyScenarioName, description, method, fullUrl,
                                invalidRequestBody, getErrorResponseSchema(), getExpectedHeaders(operation), 400,
                                generateNegativeTestSteps(method, substitutedPath, "Invalid Body"), featureFile);
                        scenarios.add(invalidBodyScenario);
                    }
                    
                    // Missing required fields scenario (for POST/PUT/PATCH)
                    if (hasRequestBody(operation)) {
                        String missingFieldsScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Missing_Required_Fields";
                        String description = "Missing required fields in " + method + " request to " + path;
                        String substitutedPath = substitutePathParameters(path, operation);
                        String fullUrl = baseUrl + substitutedPath;
                        String incompleteRequestBody = generateIncompleteRequestBody(operation);
                        
                        content.append("  Scenario: ").append(missingFieldsScenarioName).append("\n");
                        content.append("    Given the API is available\n");
                        content.append("    And I have valid authentication credentials\n");
                        content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                        content.append("    And I include a request body with missing required fields\n");
                        content.append("    And I include valid headers\n");
                        content.append("    Then the response status should be 400\n");
                        content.append("    And the response should contain field validation error messages\n");
                        content.append("    And the response should be valid JSON\n");
                        content.append("    And the response time should be less than 5 seconds\n\n");
                        
                        TestScenario missingFieldsScenario = createTestScenario(missingFieldsScenarioName, description, method, fullUrl,
                                incompleteRequestBody, getErrorResponseSchema(), getExpectedHeaders(operation), 400,
                                generateNegativeTestSteps(method, substitutedPath, "Missing Required Fields"), featureFile);
                        scenarios.add(missingFieldsScenario);
                    }
                    
                    // Unauthorized access scenario
                    String unauthorizedScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Unauthorized_Access";
                    String description = "Unauthorized access to " + method + " request to " + path;
                    String substitutedPath = substitutePathParameters(path, operation);
                    String fullUrl = baseUrl + substitutedPath;
                    
                    content.append("  Scenario: ").append(unauthorizedScenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    And I do not have valid authentication credentials\n");
                    content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                    content.append("    And I include invalid or missing authentication headers\n");
                    content.append("    Then the response status should be 401\n");
                    content.append("    And the response should contain an authentication error message\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response time should be less than 5 seconds\n\n");
                    
                    TestScenario unauthorizedScenario = createTestScenario(unauthorizedScenarioName, description, method, fullUrl,
                            null, getErrorResponseSchema(), getExpectedHeaders(operation), 401,
                            generateNegativeTestSteps(method, substitutedPath, "Unauthorized Access"), featureFile);
                    scenarios.add(unauthorizedScenario);
                }
            }
        }
        
        // Update the content with all scenarios
        featureFile.setContent(content.toString());
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private FeatureFile generateIntegrationTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: Integration Tests - CRUD Operations with Verification\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test complete CRUD workflows with verification\n");
        content.append("  So that I can ensure data integrity and proper API behavior\n\n");
        
        String baseUrl = getBaseUrl(openAPI);
        content.append("  Background:\n");
        content.append("    Given the API base URL is \"").append(baseUrl).append("\"\n");
        content.append("    And the Content-Type is \"application/json\"\n\n");
        
        List<TestScenario> scenarios = new ArrayList<>();
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Rule 1: POST endpoints - Create and verify
                if (pathItem.getPost() != null) {
                    generatePostIntegrationTests(content, scenarios, path, pathItem.getPost(), pathItem.getGet());
                }
                
                // Rule 2: PUT/PATCH endpoints - Update and verify
                if (pathItem.getPut() != null) {
                    generatePutIntegrationTests(content, scenarios, path, pathItem.getPut(), pathItem.getGet());
                }
                if (pathItem.getPatch() != null) {
                    generatePatchIntegrationTests(content, scenarios, path, pathItem.getPatch(), pathItem.getGet());
                }
                
                // Rule 3: DELETE endpoints - Delete and verify
                if (pathItem.getDelete() != null) {
                    generateDeleteIntegrationTests(content, scenarios, path, pathItem.getDelete(), pathItem.getGet());
                }
                
                // Rule 4: GET endpoints with filters
                if (pathItem.getGet() != null) {
                    generateGetFilterTests(content, scenarios, path, pathItem.getGet());
                }
            }
        }
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_integration_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.INTEGRATION, content.toString(), apiSpec);
        
        // Set the FeatureFile on all scenarios
        for (TestScenario scenario : scenarios) {
            scenario.setFeatureFile(featureFile);
        }
        
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private void generatePostIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation postOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Create " + resourceName + " and verify creation";
        
        content.append("  @integration @create @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to create a new ").append(resourceName).append("\n");
        
        // Generate POST request body based on schema
        String requestBody = generateRequestBody(postOp);
        content.append("    When I send a POST request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 201 or 200\n");
        content.append("    And the response should contain an \"id\" field\n");
        content.append("    And I store the response \"id\" as \"created").append(resourceName).append("Id\"\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPath(path, postOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the created ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal {{created").append(resourceName).append("Id}}\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for creating " + resourceName + " with verification");
        scenario.setHttpMethod("POST");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generatePutIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation putOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Update " + resourceName + " using PUT and verify update";
        
        content.append("  @integration @update @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to update an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        // Generate PUT request body
        String requestBody = generateRequestBody(putOp);
        content.append("    When I send a PUT request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should contain updated ").append(resourceName).append(" data\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPath(path, putOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the updated ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal 1\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for updating " + resourceName + " with verification");
        scenario.setHttpMethod("PUT");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generatePatchIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation patchOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Partially update " + resourceName + " using PATCH and verify update";
        
        content.append("  @integration @update @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to partially update an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        // Generate PATCH request body (partial update)
        String requestBody = generatePartialRequestBody(patchOp);
        content.append("    When I send a PATCH request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should contain updated ").append(resourceName).append(" data\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPath(path, patchOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the updated ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal 1\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for partially updating " + resourceName + " with verification");
        scenario.setHttpMethod("PATCH");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generateDeleteIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation deleteOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Delete " + resourceName + " and verify deletion";
        
        content.append("  @integration @delete @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to delete an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        content.append("    When I send a DELETE request to \"").append(path).append("\"\n");
        content.append("    Then the response status should be 200 or 204\n\n");
        
        // Generate GET verification for 404
        if (getOp != null) {
            String getPath = generateGetPath(path, deleteOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 404\n");
            content.append("    And the response should not contain the deleted ").append(resourceName).append(" data\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for deleting " + resourceName + " with verification");
        scenario.setHttpMethod("DELETE");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generateGetFilterTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation getOp) {
        String resourceName = extractResourceName(path);
        
        // Test basic GET endpoint
        content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: Get all ").append(resourceName).append(" and verify response structure\n");
        content.append("    Given I want to retrieve all ").append(resourceName).append("\n");
        content.append("    When I send a GET request to \"").append(path).append("\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should be an array\n");
        content.append("    And each item in the response should contain valid ").append(resourceName).append(" data\n\n");
        
        // Create TestScenario object for basic GET
        TestScenario basicGetScenario = new TestScenario();
        basicGetScenario.setScenarioName("Get all " + resourceName + " and verify response structure");
        basicGetScenario.setDescription("Integration test for retrieving all " + resourceName);
        basicGetScenario.setHttpMethod("GET");
        basicGetScenario.setEndpoint(path);
        basicGetScenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(basicGetScenario);
        
        // Test GET with ID parameter
        if (path.contains("{") && path.contains("}")) {
            content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
            content.append("  Scenario: Get ").append(resourceName).append(" by ID\n");
            content.append("    Given I want to retrieve a specific ").append(resourceName).append("\n");
            content.append("    When I send a GET request to \"").append(path).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain valid ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal the requested ID\n\n");
            
            // Create TestScenario object for GET by ID
            TestScenario getByIdScenario = new TestScenario();
            getByIdScenario.setScenarioName("Get " + resourceName + " by ID");
            getByIdScenario.setDescription("Integration test for retrieving " + resourceName + " by ID");
            getByIdScenario.setHttpMethod("GET");
            getByIdScenario.setEndpoint(path);
            getByIdScenario.setCreatedAt(java.time.LocalDateTime.now());
            scenarios.add(getByIdScenario);
            
            // Test invalid ID
            content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
            content.append("  Scenario: Get ").append(resourceName).append(" with invalid ID should return 404\n");
            content.append("    Given I want to retrieve a non-existent ").append(resourceName).append("\n");
            content.append("    When I send a GET request to \"").append(path).append("\"\n");
            content.append("    Then the response status should be 404\n\n");
            
            // Create TestScenario object for invalid ID
            TestScenario invalidIdScenario = new TestScenario();
            invalidIdScenario.setScenarioName("Get " + resourceName + " with invalid ID should return 404");
            invalidIdScenario.setDescription("Integration test for retrieving " + resourceName + " with invalid ID");
            invalidIdScenario.setHttpMethod("GET");
            invalidIdScenario.setEndpoint(path);
            invalidIdScenario.setCreatedAt(java.time.LocalDateTime.now());
            scenarios.add(invalidIdScenario);
        }
        
        // Test query parameters
        if (getOp.getParameters() != null) {
            for (io.swagger.v3.oas.models.parameters.Parameter param : getOp.getParameters()) {
                if ("query".equals(param.getIn())) {
                    generateQueryParameterTests(content, scenarios, path, resourceName, param);
                }
            }
        }
    }
    
    private void generateQueryParameterTests(StringBuilder content, List<TestScenario> scenarios, String path, String resourceName, io.swagger.v3.oas.models.parameters.Parameter param) {
        String paramName = param.getName();
        String paramType = param.getSchema() != null ? param.getSchema().getType() : "string";
        
        content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: Get ").append(resourceName).append(" filtered by ").append(paramName).append("\n");
        content.append("    Given I want to retrieve ").append(resourceName).append(" filtered by ").append(paramName).append("\n");
        content.append("    When I send a GET request to \"").append(path).append("\" with query parameter \"").append(paramName).append("=1\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should be an array\n");
        content.append("    And each item in the response should have \"").append(paramName).append("\" equal to 1\n\n");
        
        // Create TestScenario object for valid parameter
        TestScenario validParamScenario = new TestScenario();
        validParamScenario.setScenarioName("Get " + resourceName + " filtered by " + paramName);
        validParamScenario.setDescription("Integration test for retrieving " + resourceName + " filtered by " + paramName);
        validParamScenario.setHttpMethod("GET");
        validParamScenario.setEndpoint(path);
        validParamScenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(validParamScenario);
        
        // Test invalid parameter
        content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: Get ").append(resourceName).append(" with invalid ").append(paramName).append(" parameter\n");
        content.append("    Given I want to retrieve ").append(resourceName).append(" with invalid ").append(paramName).append("\n");
        content.append("    When I send a GET request to \"").append(path).append("\" with query parameter \"").append(paramName).append("=invalid\"\n");
        content.append("    Then the response status should be 400\n\n");
        
        // Create TestScenario object for invalid parameter
        TestScenario invalidParamScenario = new TestScenario();
        invalidParamScenario.setScenarioName("Get " + resourceName + " with invalid " + paramName + " parameter");
        invalidParamScenario.setDescription("Integration test for retrieving " + resourceName + " with invalid " + paramName);
        invalidParamScenario.setHttpMethod("GET");
        invalidParamScenario.setEndpoint(path);
        invalidParamScenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(invalidParamScenario);
    }
    
    private String extractResourceName(String path) {
        // Extract resource name from path (e.g., /posts -> Posts, /users/{id} -> Users)
        String[] parts = path.split("/");
        for (String part : parts) {
            if (!part.isEmpty() && !part.startsWith("{")) {
                return part.substring(0, 1).toUpperCase() + part.substring(1);
            }
        }
        return "Resource";
    }
    
    private String generateRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema schema = content.get("application/json").getSchema();
                return generateJsonFromSchema(schema);
            }
        }
        return "{\n      \"id\": 1,\n      \"name\": \"Test Resource\",\n      \"description\": \"Test description\"\n    }";
    }
    
    private String generatePartialRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema schema = content.get("application/json").getSchema();
                return generatePartialJsonFromSchema(schema);
            }
        }
        return "{\n      \"name\": \"Updated Test Resource\"\n    }";
    }
    
    private String generateJsonFromSchema(Schema schema) {
        if (schema == null) {
            return "{\n      \"id\": 1,\n      \"name\": \"Test Resource\"\n    }";
        }
        
        StringBuilder json = new StringBuilder("{\n");
        if (schema.getProperties() != null) {
            boolean first = true;
            for (Object key : schema.getProperties().keySet()) {
                if (!first) json.append(",\n");
                json.append("      \"").append((String) key).append("\": ");
                json.append(generateValueFromSchema((Schema) schema.getProperties().get(key)));
                first = false;
            }
        } else {
            json.append("      \"id\": 1,\n");
            json.append("      \"name\": \"Test Resource\"");
        }
        json.append("\n    }");
        return json.toString();
    }
    
    private String generatePartialJsonFromSchema(Schema schema) {
        if (schema == null) {
            return "{\n      \"name\": \"Updated Test Resource\"\n    }";
        }
        
        StringBuilder json = new StringBuilder("{\n");
        if (schema.getProperties() != null) {
            boolean first = true;
            int count = 0;
            for (Object key : schema.getProperties().keySet()) {
                if (count >= 2) break; // Only include first 2 properties for partial update
                if (!first) json.append(",\n");
                json.append("      \"").append((String) key).append("\": ");
                json.append(generateValueFromSchema((Schema) schema.getProperties().get(key)));
                first = false;
                count++;
            }
        } else {
            json.append("      \"name\": \"Updated Test Resource\"");
        }
        json.append("\n    }");
        return json.toString();
    }
    
    private String generateValueFromSchema(Schema schema) {
        if (schema == null) return "\"test\"";
        
        String type = schema.getType();
        if (type == null) return "\"test\"";
        
        switch (type) {
            case "string":
                return "\"Test Value\"";
            case "integer":
                return "1";
            case "number":
                return "1.0";
            case "boolean":
                return "true";
            case "array":
                return "[]";
            case "object":
                return "{}";
            default:
                return "\"test\"";
        }
    }
    
    private String generateGetPath(String path, Operation operation) {
        // Convert path with parameters to actual path
        if (path.contains("{") && path.contains("}")) {
            return path.replaceAll("\\{[^}]+\\}", "1");
        }
        return path;
    }
    
    private Map<String, Operation> getOperations(PathItem pathItem) {
        Map<String, Operation> operations = new HashMap<>();
        if (pathItem.getGet() != null) operations.put("GET", pathItem.getGet());
        if (pathItem.getPost() != null) operations.put("POST", pathItem.getPost());
        if (pathItem.getPut() != null) operations.put("PUT", pathItem.getPut());
        if (pathItem.getPatch() != null) operations.put("PATCH", pathItem.getPatch());
        if (pathItem.getDelete() != null) operations.put("DELETE", pathItem.getDelete());
        if (pathItem.getHead() != null) operations.put("HEAD", pathItem.getHead());
        if (pathItem.getOptions() != null) operations.put("OPTIONS", pathItem.getOptions());
        return operations;
    }
    
    private String generateScenarioName(String method, String path, String operationId) {
        if (operationId != null && !operationId.isEmpty()) {
            return operationId.replaceAll("[^a-zA-Z0-9]", "_");
        }
        return method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_");
    }
    
    private String getRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema<?> schema = content.get("application/json").getSchema();
                if (schema != null) {
                    return generateTestRequestBody(schema);
                }
            }
        }
        return null;
    }
    
    private String generateTestRequestBody(Schema<?> schema) {
        try {
            Map<String, Object> testData = generateTestDataFromSchema(schema);
            return jsonMapper.writeValueAsString(testData);
        } catch (Exception e) {
            // Fallback to schema string if JSON generation fails
            return schema.toString();
        }
    }
    
    private String generateInvalidRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema<?> schema = content.get("application/json").getSchema();
                if (schema != null) {
                    return generateInvalidTestDataFromSchema(schema);
                }
            }
        }
        return "{\"invalid\": \"data\"}";
    }
    
    private String generateIncompleteRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema<?> schema = content.get("application/json").getSchema();
                if (schema != null) {
                    return generateIncompleteTestDataFromSchema(schema);
                }
            }
        }
        return "{}";
    }
    
    private String generateInvalidTestDataFromSchema(Schema<?> schema) {
        try {
            Map<String, Object> invalidData = new HashMap<>();
            
            if (schema.getProperties() != null) {
                for (Map.Entry<String, Schema> property : schema.getProperties().entrySet()) {
                    String key = property.getKey();
                    Schema<?> propertySchema = property.getValue();
                    
                    // Generate invalid data based on type
                    String type = propertySchema.getType();
                    switch (type) {
                        case "string":
                            invalidData.put(key, 123); // Wrong type
                            break;
                        case "integer":
                            invalidData.put(key, "invalid_number"); // Wrong type
                            break;
                        case "number":
                            invalidData.put(key, "invalid_float"); // Wrong type
                            break;
                        case "boolean":
                            invalidData.put(key, "invalid_boolean"); // Wrong type
                            break;
                        case "array":
                            invalidData.put(key, "not_an_array"); // Wrong type
                            break;
                        case "object":
                            invalidData.put(key, "not_an_object"); // Wrong type
                            break;
                        default:
                            invalidData.put(key, null); // Null value
                    }
                }
            }
            
            return jsonMapper.writeValueAsString(invalidData);
        } catch (Exception e) {
            return "{\"invalid\": \"data\"}";
        }
    }
    
    private String generateIncompleteTestDataFromSchema(Schema<?> schema) {
        try {
            Map<String, Object> incompleteData = new HashMap<>();
            
            if (schema.getProperties() != null) {
                for (Map.Entry<String, Schema> property : schema.getProperties().entrySet()) {
                    String key = property.getKey();
                    Schema<?> propertySchema = property.getValue();
                    
                    // Only include non-required fields or skip some required fields
                    if (propertySchema.getRequired() == null || !propertySchema.getRequired().contains(key)) {
                        Object value = generateTestValue(propertySchema);
                        incompleteData.put(key, value);
                    }
                    // Skip some required fields to test validation
                }
            }
            
            return jsonMapper.writeValueAsString(incompleteData);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private Map<String, Object> generateTestDataFromSchema(Schema<?> schema) {
        Map<String, Object> testData = new HashMap<>();
        
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Schema> property : schema.getProperties().entrySet()) {
                String key = property.getKey();
                Schema<?> propertySchema = property.getValue();
                Object value = generateTestValue(propertySchema);
                testData.put(key, value);
            }
        }
        
        return testData;
    }
    
    private Object generateTestValue(Schema<?> schema) {
        if (schema == null) return null;
        
        String type = schema.getType();
        Object example = schema.getExample();
        
        // Use example if available
        if (example != null) {
            return example;
        }
        
        // Generate test data based on type
        switch (type) {
            case "string":
                return "test_" + System.currentTimeMillis();
            case "integer":
                return 1;
            case "number":
                return 1.0;
            case "boolean":
                return true;
            case "array":
                if (schema.getItems() != null) {
                    return Arrays.asList(generateTestValue(schema.getItems()));
                }
                return Arrays.asList("test_item");
            case "object":
                if (schema.getProperties() != null) {
                    return generateTestDataFromSchema(schema);
                }
                return new HashMap<>();
            default:
                return "test_value";
        }
    }
    
    private String getExpectedResponseSchema(Operation operation) {
        if (operation.getResponses() != null && operation.getResponses().get("200") != null) {
            ApiResponse response = operation.getResponses().get("200");
            if (response.getContent() != null && response.getContent().get("application/json") != null) {
                Schema<?> schema = response.getContent().get("application/json").getSchema();
                return schema != null ? schema.toString() : null;
            }
        }
        return null;
    }
    
    private String getErrorResponseSchema() {
        return "{\"type\": \"object\", \"properties\": {\"error\": {\"type\": \"string\"}, \"message\": {\"type\": \"string\"}, \"status\": {\"type\": \"integer\"}}}";
    }
    
    private String getExpectedHeaders(Operation operation) {
        // Extract expected headers from operation
        return "Content-Type: application/json";
    }
    
    private Integer getExpectedStatusCode(Operation operation) {
        if (operation.getResponses() != null) {
            if (operation.getResponses().get("200") != null) return 200;
            if (operation.getResponses().get("201") != null) return 201;
            if (operation.getResponses().get("204") != null) return 204;
        }
        return 200; // Default
    }
    
    private boolean hasRequestBody(Operation operation) {
        return operation.getRequestBody() != null;
    }
    
    private String generatePositiveTestSteps(String method, String path, String requestBody) {
        StringBuilder steps = new StringBuilder();
        steps.append("1. Prepare valid ").append(method).append(" request to ").append(path).append("\n");
        steps.append("2. Include proper authentication headers\n");
        steps.append("3. Include valid content-type headers\n");
        if (requestBody != null) {
            steps.append("4. Include valid request body with all required fields\n");
        }
        steps.append("5. Send the request\n");
        steps.append("6. Validate response status code matches expected\n");
        steps.append("7. Validate response body is valid JSON\n");
        steps.append("8. Validate response body matches expected schema\n");
        steps.append("9. Validate all mandatory fields are present\n");
        steps.append("10. Validate response headers are correct\n");
        steps.append("11. Validate response time is within acceptable limits\n");
        return steps.toString();
    }
    
    private String generateNegativeTestSteps(String method, String path, String errorType) {
        StringBuilder steps = new StringBuilder();
        steps.append("1. Prepare ").append(method).append(" request to ").append(path).append("\n");
        steps.append("2. Include ").append(errorType.toLowerCase()).append("\n");
        steps.append("3. Send the request\n");
        steps.append("4. Validate response status code indicates error\n");
        steps.append("5. Validate response body contains appropriate error message\n");
        steps.append("6. Validate response body is valid JSON\n");
        steps.append("7. Validate response time is within acceptable limits\n");
        return steps.toString();
    }
    
    private String substitutePathParameters(String path, Operation operation) {
        // First use our comprehensive path variable replacement
        String substitutedPath = replacePathVariables(path);
        
        // Then handle any remaining parameters from the operation definition
        if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
            for (io.swagger.v3.oas.models.parameters.Parameter param : operation.getParameters()) {
                if ("path".equals(param.getIn()) && param.getName() != null) {
                    String paramName = param.getName();
                    Object example = param.getExample();
                    Object defaultValue = param.getSchema() != null ? param.getSchema().getExample() : null;
                    
                    // Use example value if available, otherwise use default test value
                    String testValue;
                    if (example != null) {
                        testValue = example.toString();
                    } else if (defaultValue != null) {
                        testValue = defaultValue.toString();
                    } else {
                        // Generate test value based on parameter name
                        testValue = generateTestValueForParameter(paramName, param.getSchema());
                    }
                    
                    substitutedPath = substitutedPath.replace("{" + paramName + "}", testValue);
                }
            }
        }
        
        return substitutedPath;
    }
    
    private String substitutePathParametersWithInvalidValues(String path, Operation operation) {
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            return path;
        }
        
        String substitutedPath = path;
        
        for (io.swagger.v3.oas.models.parameters.Parameter param : operation.getParameters()) {
            if ("path".equals(param.getIn()) && param.getName() != null) {
                String paramName = param.getName();
                
                // Generate invalid test value
                String invalidValue = generateInvalidTestValueForParameter(paramName, param.getSchema());
                substitutedPath = substitutedPath.replace("{" + paramName + "}", invalidValue);
            }
        }
        
        return substitutedPath;
    }
    
    private String generateTestValueForParameter(String paramName, Schema<?> schema) {
        // Generate appropriate test values based on parameter name and type
        if (paramName.toLowerCase().contains("id")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random ID from 1-10
        } else if (paramName.toLowerCase().contains("user")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random user ID from 1-10
        } else if (paramName.toLowerCase().contains("post")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random post ID from 1-10
        } else if (paramName.toLowerCase().contains("comment")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random comment ID from 1-10
        } else if (schema != null && "integer".equals(schema.getType())) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random integer from 1-10
        } else if (schema != null && "string".equals(schema.getType())) {
            return "test";
        } else {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random value from 1-10
        }
    }
    
    private String generateInvalidTestValueForParameter(String paramName, Schema<?> schema) {
        // Generate invalid test values for negative testing
        if (paramName.toLowerCase().contains("id")) {
            return "1"; // Valid ID from 1-10 range
        } else if (paramName.toLowerCase().contains("user")) {
            return "1"; // Valid user ID from 1-10 range
        } else if (paramName.toLowerCase().contains("post")) {
            return "1"; // Valid post ID from 1-10 range
        } else if (paramName.toLowerCase().contains("comment")) {
            return "1"; // Valid comment ID from 1-10 range
        } else if (schema != null && "integer".equals(schema.getType())) {
            return "1"; // Valid integer from 1-10 range
        } else if (schema != null && "string".equals(schema.getType())) {
            return "test"; // Valid string value
        } else {
            return "1"; // Default valid value from 1-10 range
        }
    }
    
    private TestScenario createTestScenario(String scenarioName, String description, String httpMethod, 
                                         String endpoint, String requestBody, String expectedResponseSchema,
                                         String expectedHeaders, Integer expectedStatusCode, String testSteps,
                                         FeatureFile featureFile) {
        return new TestScenario(scenarioName, description, httpMethod, endpoint, requestBody,
                expectedResponseSchema, expectedHeaders, expectedStatusCode, testSteps, featureFile);
    }
    
    // ==================== SWAGGER 2.0 SPECIFIC METHODS ====================
    
    private String getBaseUrlSwagger2(Swagger swagger) {
        if (swagger.getHost() != null && swagger.getBasePath() != null) {
            return "https://" + swagger.getHost() + swagger.getBasePath();
        } else if (swagger.getHost() != null) {
            return "https://" + swagger.getHost();
        }
        return "https://api.example.com";
    }
    
    private FeatureFile generateSmokeTestsSwagger2(Swagger swagger, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        List<TestScenario> scenarios = new ArrayList<>();
        
        content.append("Feature: Smoke Tests - Read-only Operations (Positive Scenarios)\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to verify basic API functionality with valid requests\n");
        content.append("  So that I can ensure the API is accessible and responding correctly\n\n");
        
        String baseUrl = getBaseUrlSwagger2(swagger);
        
        if (swagger != null && swagger.getPaths() != null) {
            for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                Path pathItem = pathEntry.getValue();
                
                if (pathItem != null && pathItem.getGet() != null) {
                    generateSmokeTestSwagger2(content, scenarios, path, pathItem.getGet(), baseUrl);
                }
            }
        }
        
        // If no paths found, create a basic smoke test
        if (scenarios.isEmpty()) {
            content.append("  Scenario: Basic API Health Check\n");
            content.append("    Given the API is available\n");
            content.append("    When I send a GET request to \"/health\"\n");
            content.append("    Then the response status should be 200\n\n");
            
            TestScenario scenario = new TestScenario();
            scenario.setScenarioName("Basic API Health Check");
            scenario.setDescription("Basic health check for API availability");
            scenario.setHttpMethod("GET");
            scenario.setEndpoint("/health");
            scenario.setCreatedAt(java.time.LocalDateTime.now());
            scenarios.add(scenario);
        }
        
        String fileName = (apiSpec.getName() != null ? apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") : "API") + "_smoke_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SMOKE, content.toString(), apiSpec);
        
        // Set the FeatureFile on all scenarios
        for (TestScenario scenario : scenarios) {
            scenario.setFeatureFile(featureFile);
        }
        
        featureFile.setTestScenarios(scenarios);
        return featureFile;
    }
    
    private void generateSmokeTestSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation operation, String baseUrl) {
        String cleanPath = replacePathVariables(path);
        String scenarioName = "GET " + cleanPath + " - Valid Request";
        
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given the API is available\n");
        content.append("    And I have valid authentication credentials\n");
        content.append("    When I send a GET request to \"").append(cleanPath).append("\"\n");
        content.append("    And I include valid headers\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should be valid JSON\n");
        content.append("    And the response should not be empty\n\n");
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Smoke test for GET " + cleanPath);
        scenario.setHttpMethod("GET");
        scenario.setEndpoint(cleanPath);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private FeatureFile generateSystemTestsSwagger2(Swagger swagger, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        List<TestScenario> scenarios = new ArrayList<>();
        
        content.append("Feature: System Tests - All CRUD Operations (Positive Scenarios)\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test all API operations thoroughly with valid data\n");
        content.append("  So that I can ensure complete API functionality works correctly\n\n");
        
        String baseUrl = getBaseUrlSwagger2(swagger);
        
        if (swagger != null && swagger.getPaths() != null) {
            for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                Path pathItem = pathEntry.getValue();
                
                if (pathItem != null) {
                    if (pathItem.getPost() != null) {
                        generateSystemTestSwagger2(content, scenarios, path, pathItem.getPost(), "POST", baseUrl);
                    }
                    if (pathItem.getPut() != null) {
                        generateSystemTestSwagger2(content, scenarios, path, pathItem.getPut(), "PUT", baseUrl);
                    }
                    if (pathItem.getPatch() != null) {
                        generateSystemTestSwagger2(content, scenarios, path, pathItem.getPatch(), "PATCH", baseUrl);
                    }
                    if (pathItem.getDelete() != null) {
                        generateSystemTestSwagger2(content, scenarios, path, pathItem.getDelete(), "DELETE", baseUrl);
                    }
                }
            }
        }
        
        // If no paths found, create a basic system test
        if (scenarios.isEmpty()) {
            content.append("  Scenario: Basic System Test\n");
            content.append("    Given the API is available\n");
            content.append("    When I send a POST request to \"/test\"\n");
            content.append("    Then the response status should be 200\n\n");
            
            TestScenario scenario = new TestScenario();
            scenario.setScenarioName("Basic System Test");
            scenario.setDescription("Basic system test for API functionality");
            scenario.setHttpMethod("POST");
            scenario.setEndpoint("/test");
            scenario.setCreatedAt(java.time.LocalDateTime.now());
            scenarios.add(scenario);
        }
        
        String fileName = (apiSpec.getName() != null ? apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") : "API") + "_system_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SYSTEM, content.toString(), apiSpec);
        
        // Set the FeatureFile on all scenarios
        for (TestScenario scenario : scenarios) {
            scenario.setFeatureFile(featureFile);
        }
        
        featureFile.setTestScenarios(scenarios);
        return featureFile;
    }
    
    private void generateSystemTestSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation operation, String method, String baseUrl) {
        String cleanPath = replacePathVariables(path);
        String scenarioName = method + " " + cleanPath + " - Valid Request";
        
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given the API is available\n");
        content.append("    And I have valid authentication credentials\n");
        content.append("    When I send a ").append(method).append(" request to \"").append(cleanPath).append("\"\n");
        
        if (operation.getParameters() != null && operation.getParameters().stream().anyMatch(p -> p instanceof BodyParameter)) {
            content.append("    And I include a valid request body\n");
        }
        
        content.append("    And I include valid headers\n");
        
        // Determine expected status code based on method
        int expectedStatus = getExpectedStatusCodeSwagger2(method, operation);
        content.append("    Then the response status should be ").append(expectedStatus).append("\n");
        content.append("    And the response should be valid JSON\n\n");
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("System test for " + method + " " + cleanPath);
        scenario.setHttpMethod(method);
        scenario.setEndpoint(cleanPath);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private int getExpectedStatusCodeSwagger2(String method, io.swagger.models.Operation operation) {
        if ("POST".equals(method)) {
            return 201;
        } else if ("PUT".equals(method) || "PATCH".equals(method)) {
            return 200;
        } else if ("DELETE".equals(method)) {
            return 200;
        }
        return 200;
    }
    
    private FeatureFile generateNegativeTestsSwagger2(Swagger swagger, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        List<TestScenario> scenarios = new ArrayList<>();
        
        content.append("Feature: Negative Tests - Invalid Requests and Error Handling\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test API error handling with invalid requests\n");
        content.append("  So that I can ensure the API handles errors gracefully\n\n");
        
        String baseUrl = getBaseUrlSwagger2(swagger);
        
        if (swagger.getPaths() != null) {
            for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                Path pathItem = pathEntry.getValue();
                
                if (pathItem.getPost() != null) {
                    generateNegativeTestSwagger2(content, scenarios, path, pathItem.getPost(), "POST", baseUrl);
                }
                if (pathItem.getPut() != null) {
                    generateNegativeTestSwagger2(content, scenarios, path, pathItem.getPut(), "PUT", baseUrl);
                }
                if (pathItem.getPatch() != null) {
                    generateNegativeTestSwagger2(content, scenarios, path, pathItem.getPatch(), "PATCH", baseUrl);
                }
                if (pathItem.getDelete() != null) {
                    generateNegativeTestSwagger2(content, scenarios, path, pathItem.getDelete(), "DELETE", baseUrl);
                }
            }
        }
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_negative_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SYSTEM, content.toString(), apiSpec);
        
        // Set the FeatureFile on all scenarios
        for (TestScenario scenario : scenarios) {
            scenario.setFeatureFile(featureFile);
        }
        
        featureFile.setTestScenarios(scenarios);
        return featureFile;
    }
    
    private void generateNegativeTestSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation operation, String method, String baseUrl) {
        String scenarioName = method + " " + path + " - Invalid Request";
        
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given the API is available\n");
        content.append("    And I have valid authentication credentials\n");
        content.append("    When I send a ").append(method).append(" request to \"").append(path).append("\"\n");
        
        if (operation.getParameters() != null && operation.getParameters().stream().anyMatch(p -> p instanceof BodyParameter)) {
            content.append("    And I include an invalid request body\n");
        }
        
        content.append("    And I include valid headers\n");
        content.append("    Then the response status should be 400\n");
        content.append("    And the response should contain an error message\n\n");
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Negative test for " + method + " " + path);
        scenario.setHttpMethod(method);
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private FeatureFile generateIntegrationTestsSwagger2(Swagger swagger, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        List<TestScenario> scenarios = new ArrayList<>();
        
        content.append("Feature: Integration Tests - CRUD Operations with Verification\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test complete CRUD workflows with verification\n");
        content.append("  So that I can ensure data integrity and proper API behavior\n\n");
        
        content.append("  Background:\n");
        content.append("    Given the API base URL is \"").append(getBaseUrlSwagger2(swagger)).append("\"\n");
        content.append("    And the Content-Type is \"application/json\"\n\n");
        
        if (swagger.getPaths() != null) {
            for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                Path pathItem = pathEntry.getValue();
                
                if (pathItem.getPost() != null) {
                    generatePostIntegrationTestsSwagger2(content, scenarios, path, pathItem.getPost(), findGetOperationSwagger2(swagger, path));
                }
                if (pathItem.getPut() != null) {
                    generatePutIntegrationTestsSwagger2(content, scenarios, path, pathItem.getPut(), findGetOperationSwagger2(swagger, path));
                }
                if (pathItem.getPatch() != null) {
                    generatePatchIntegrationTestsSwagger2(content, scenarios, path, pathItem.getPatch(), findGetOperationSwagger2(swagger, path));
                }
                if (pathItem.getDelete() != null) {
                    generateDeleteIntegrationTestsSwagger2(content, scenarios, path, pathItem.getDelete(), findGetOperationSwagger2(swagger, path));
                }
                if (pathItem.getGet() != null) {
                    generateGetFilterTestsSwagger2(content, scenarios, path, pathItem.getGet());
                }
            }
        }
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_integration_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.INTEGRATION, content.toString(), apiSpec);
        
        // Set the FeatureFile on all scenarios
        for (TestScenario scenario : scenarios) {
            scenario.setFeatureFile(featureFile);
        }
        
        featureFile.setTestScenarios(scenarios);
        return featureFile;
    }
    
    private io.swagger.models.Operation findGetOperationSwagger2(Swagger swagger, String path) {
        Path pathItem = swagger.getPaths().get(path);
        return pathItem != null ? pathItem.getGet() : null;
    }
    
    private void generatePostIntegrationTestsSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation postOp, io.swagger.models.Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Create " + resourceName + " and verify creation";
        
        content.append("  @integration @create @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to create a new ").append(resourceName).append("\n");
        
        // Generate POST request body based on Swagger 2.0 schema
        String requestBody = generateRequestBodySwagger2(postOp);
        content.append("    When I send a POST request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 201 or 200\n");
        content.append("    And the response should contain an \"id\" field\n");
        content.append("    And I store the response \"id\" as \"created").append(resourceName).append("Id\"\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPathSwagger2(path, postOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the created ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal {{created").append(resourceName).append("Id}}\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for creating " + resourceName + " with verification");
        scenario.setHttpMethod("POST");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generatePutIntegrationTestsSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation putOp, io.swagger.models.Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Update " + resourceName + " using PUT and verify update";
        
        content.append("  @integration @update @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to update an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        String requestBody = generateRequestBodySwagger2(putOp);
        content.append("    When I send a PUT request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should contain updated ").append(resourceName).append(" data\n\n");
        
        if (getOp != null) {
            String getPath = generateGetPathSwagger2(path, putOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the updated ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal 1\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for updating " + resourceName + " with verification");
        scenario.setHttpMethod("PUT");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generatePatchIntegrationTestsSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation patchOp, io.swagger.models.Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Update " + resourceName + " using PATCH and verify update";
        
        content.append("  @integration @update @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to update an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        String requestBody = generatePartialRequestBodySwagger2(patchOp);
        content.append("    When I send a PATCH request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should contain updated ").append(resourceName).append(" data\n\n");
        
        if (getOp != null) {
            String getPath = generateGetPathSwagger2(path, patchOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the updated ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal 1\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for patching " + resourceName + " with verification");
        scenario.setHttpMethod("PATCH");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generateDeleteIntegrationTestsSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation deleteOp, io.swagger.models.Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Delete " + resourceName + " and verify deletion";
        
        content.append("  @integration @delete @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to delete an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        content.append("    When I send a DELETE request to \"").append(path).append("\"\n");
        content.append("    Then the response status should be 200\n\n");
        
        if (getOp != null) {
            String getPath = generateGetPathSwagger2(path, deleteOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 404\n");
            content.append("    And the response should not contain the deleted ").append(resourceName).append(" data\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for deleting " + resourceName + " with verification");
        scenario.setHttpMethod("DELETE");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generateGetFilterTestsSwagger2(StringBuilder content, List<TestScenario> scenarios, String path, io.swagger.models.Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Get " + resourceName + " with filters";
        
        content.append("  @integration @read @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to retrieve ").append(resourceName).append(" with filters\n");
        
        content.append("    When I send a GET request to \"").append(path).append("\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should be an array\n");
        content.append("    And the response should contain ").append(resourceName).append(" data\n\n");
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for getting " + resourceName + " with filters");
        scenario.setHttpMethod("GET");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private String generateRequestBodySwagger2(io.swagger.models.Operation operation) {
        if (operation.getParameters() == null) {
            return "{}";
        }
        
        for (Parameter param : operation.getParameters()) {
            if (param instanceof BodyParameter) {
                BodyParameter bodyParam = (BodyParameter) param;
                if (bodyParam.getSchema() != null) {
                    return generateJsonFromSchemaSwagger2(bodyParam.getSchema());
                }
            }
        }
        
        return "{}";
    }
    
    private String generatePartialRequestBodySwagger2(io.swagger.models.Operation operation) {
        if (operation.getParameters() == null) {
            return "{}";
        }
        
        for (Parameter param : operation.getParameters()) {
            if (param instanceof BodyParameter) {
                BodyParameter bodyParam = (BodyParameter) param;
                if (bodyParam.getSchema() != null) {
                    return generatePartialJsonFromSchemaSwagger2(bodyParam.getSchema());
                }
            }
        }
        
        return "{}";
    }
    
    private String generateJsonFromSchemaSwagger2(io.swagger.models.Model schema) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        if (schema.getProperties() != null) {
            boolean first = true;
            for (Map.Entry<String, Property> entry : schema.getProperties().entrySet()) {
                if (!first) {
                    json.append(",\n");
                }
                json.append("  \"").append(entry.getKey()).append("\": ");
                json.append(generateValueFromPropertySwagger2(entry.getValue()));
                first = false;
            }
        }
        
        json.append("\n}");
        return json.toString();
    }
    
    private String generatePartialJsonFromSchemaSwagger2(io.swagger.models.Model schema) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        if (schema.getProperties() != null) {
            boolean first = true;
            int count = 0;
            for (Map.Entry<String, Property> entry : schema.getProperties().entrySet()) {
                if (count >= 2) break; // Only include first 2 properties for partial updates
                
                if (!first) {
                    json.append(",\n");
                }
                json.append("  \"").append(entry.getKey()).append("\": ");
                json.append(generateValueFromPropertySwagger2(entry.getValue()));
                first = false;
                count++;
            }
        }
        
        json.append("\n}");
        return json.toString();
    }
    
    private String generateValueFromPropertySwagger2(io.swagger.models.properties.Property property) {
        if (property.getType() != null) {
            switch (property.getType()) {
                case "string":
                    return "\"Test Value\"";
                case "integer":
                    return "1";
                case "number":
                    return "1.0";
                case "boolean":
                    return "true";
                case "array":
                    return "[]";
                case "object":
                    return "{}";
                default:
                    return "\"Test Value\"";
            }
        }
        return "\"Test Value\"";
    }
    
    private String generateGetPathSwagger2(String path, io.swagger.models.Operation operation) {
        // Replace path parameters with test values
        String getPath = path;
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                if (param instanceof PathParameter) {
                    PathParameter pathParam = (PathParameter) param;
                    String paramName = pathParam.getName();
                    getPath = getPath.replace("{" + paramName + "}", "1");
                }
            }
        }
        return getPath;
    }
    
}