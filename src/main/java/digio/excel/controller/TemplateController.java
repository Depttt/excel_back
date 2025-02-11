package digio.excel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import digio.excel.DTO.TemplateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/save/templates")
public class TemplateController {

    private final StorageClient storageClient;
    private static final String FILE_NAME = "template.yaml";

    public TemplateController(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    @GetMapping("/load")
    public ResponseEntity<Object> loadAllTemplate(){
        Map<String, Map<String, List<Map<String, Object>>>> yamlData = readYamlFromStorage();

        if (yamlData.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found"));
        }
        return ResponseEntity.ok(yamlData);
    }

    @GetMapping("/{usertoken}")
    public  ResponseEntity<Object> getTemplateByUserToken(@PathVariable String userToken){
        try{
            Map<String, Map<String, List<Map<String,Object>>>> yamlData = readYamlFromStorage();

            if (yamlData == null || yamlData.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "❌ No templates available in storage"));
            }

            Map<String, List<Map<String,Object>>> userTemplate = yamlData.get(userToken);

            if (userTemplate == null || userTemplate.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "❌ No templates found for user" + userToken));
            }

            return ResponseEntity.ok(userTemplate);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "⚠️ Failed to load templates: " + e.getMessage()));
        }
    }

    @PostMapping("/update-token")
    public ResponseEntity<Object> UpdateUserToken(@RequestBody Map<String, Object> requestBody){
        String userToken = (String) requestBody.get("userToken");
        List<Map<String, Object>> templates = (List<Map<String, Object>>) requestBody.get("templates");

        if (userToken == null || userToken.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "❌ Invalid request data"));
        }

        Map<String, Map<String, List<Map<String, Object>>>> yamlData = readYamlFromStorage();

        if (yamlData.containsKey(userToken)){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "❌ user token is already exists"));
        }

        yamlData.put(userToken, new HashMap<>());
        yamlData.get(userToken).put("templates", templates);

        saveYamlToStorage(yamlData);

        System.out.println("✅ New user token added: " + userToken);
        System.out.println("✅ User token updated successfully");
        return ResponseEntity.ok(Map.of("message", "User token added successfully"));
    }

    @DeleteMapping("/{usertoken}/{templateName}")
    public ResponseEntity<Object> deleteTemplate(@PathVariable String userToken, @PathVariable String templateName){
        Map<String, Map<String, List<Map<String, Object>>>> yamlData = readYamlFromStorage();

        if (!yamlData.containsKey(userToken) || yamlData.get(userToken).get("templates") == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found for user " + userToken));
        }

        List<Map<String, Object>> templates = yamlData.get(userToken).get("templates");
        boolean removed = templates.removeIf(t -> templateName.equals(t.get("templatename")));

        if (!removed){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ Template not found"));
        }

        saveYamlToStorage(yamlData);
        System.out.println("✅ Template deleted successfully");
        return ResponseEntity.ok(Map.of("message", "✅ Template deleted successfully"));
    }

    @PutMapping("/{usertoken}/{templateName}")
    public ResponseEntity<Object> updateTemplate (@PathVariable String userToken, @PathVariable String templateName,
                                                  @RequestBody TemplateRequest templateRequest){
        Map<String, Map<String, List<Map<String, Object>>>> yamlData = readYamlFromStorage();

        if (!yamlData.containsKey(userToken) || yamlData.get(userToken).get("templates") == null ){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ No templates found for user " + userToken));
        }

        List<Map<String, Object>> templates = yamlData.get(userToken).get("templates");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> updatedTemplate = objectMapper.convertValue(templateRequest, Map.class);

        boolean updated = false;
        for (int i = 0; i < templates.size(); i++){
            if (templateName.equals(templates.get(i).get("templatename"))){
                templates.set(i, updatedTemplate);
                updated = true;
                break;
            }
        }

        if (!updated){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "❌ Template not found"));
        }

        saveYamlToStorage(yamlData);
        System.out.println("✅ Template updated successfully");
        return ResponseEntity.ok("✅ Template updated successfully");
    }

    @PostMapping
    public ResponseEntity<Object> saveTemplate(@RequestBody TemplateRequest templateRequest){
        Map<String, Map<String, List<Map<String, Object>>>> yamlData = readYamlFromStorage();

        String userToken = templateRequest.getUserToken();
        yamlData.putIfAbsent(userToken, new HashMap<>());
        yamlData.get(userToken).putIfAbsent("templates",new ArrayList<>());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> templateMap = objectMapper.convertValue(templateRequest, Map.class);
        List<Map<String, Object>> templates = yamlData.get(userToken).get("templates");

        boolean updated = false;
        for (int i = 0; i< templates.size(); i++){
            if (templates.get(i).get("templatename").equals(templateRequest.getTemplatename())) {
                templates.set(i, templateMap);
                updated = true;
                break;
            }
        }

        if (!updated){
            templates.add(templateMap);
        }

        saveYamlToStorage(yamlData);
        System.out.println("✅ Template saved successfully");
        return ResponseEntity.ok(Map.of("message", "✅ Template saved successfully"));
    }

    private Map<String, Map<String, List<Map<String, Object>>>> readYamlFromStorage(){
        try {
            Bucket bucket = storageClient.bucket();
            Blob blob = bucket.get(FILE_NAME);
            if (blob == null) return new HashMap<>();

            String yamlContent = new String(blob.getContent(), StandardCharsets.UTF_8);

            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setAllowRecursiveKeys(false);

            Yaml yaml = new Yaml(new Constructor(Map.class, loaderOptions));
            return yaml.load(yamlContent);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void saveYamlToStorage(Map<String, Map<String, List<Map<String, Object>>>> yamlData){
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Representer representer = new Representer(options);
            representer.addClassTag(Map.class, Tag.MAP);

            Yaml yaml = new Yaml(representer, options);
            String yamlString = yaml.dump(yamlData);

            storageClient.bucket().create(FILE_NAME, yamlString.getBytes(StandardCharsets.UTF_8));
            System.out.println("YAML file successfully saved.");

        } catch (Exception e) {
            System.err.println("Errror saving YAML file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
