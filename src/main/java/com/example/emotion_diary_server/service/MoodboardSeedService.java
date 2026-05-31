package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.dto.MoodboardContentDto;
import com.example.emotion_diary_server.dto.MoodboardElementDto;
import com.example.emotion_diary_server.model.Moodboard;
import com.example.emotion_diary_server.persistence.EntityReferences;
import com.example.emotion_diary_server.repository.MoodboardRepository;
import com.example.emotion_diary_server.user.User;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class MoodboardSeedService {

    private static final String[] MOOD_NAMES = {
            "Calm morning", "Rainy day", "Golden hour", "Midnight thoughts", "Spring bloom",
            "Ocean breeze", "Cozy corner", "Urban energy", "Soft pastels", "Bold contrast",
            "Nostalgia", "Fresh start", "Dreamy clouds", "Warm sunset", "Quiet night",
            "Creative spark", "Weekend vibes", "Gratitude", "Hopeful dawn", "Gentle rain"
    };

    private static final String[] TEXT_SNIPPETS = {
            "Today I feel grateful", "Breathe and let go", "Small joys matter",
            "This moment is enough", "Colors of my mood", "Finding balance",
            "Notes to self", "What made me smile", "Energy check-in", "A peaceful pause",
            "Creative flow", "Looking forward", "Reflect and reset", "Heart full",
            "Taking it slow", "Bright ideas", "Soft focus", "Inner calm", "New perspective"
    };

    private static final String[] BACKGROUNDS = {
            "#ffffff", "#f8f4ef", "#e8f4fc", "#fce8e8", "#f0fce8", "#f5f0ff", "#fff8e8", "#e8fcf5"
    };

    private static final String[] COLORS = {
            "#1a1a2e", "#4a90d9", "#e85d75", "#2ecc71", "#9b59b6", "#f39c12", "#34495e", "#16a085"
    };

    private static final String[] FILLS = {
            "#e8f4fc", "#fce8e8", "#f0fce8", "#f5f0ff", "#fff8e8", "#ffe8f0", "#e8fcf5", "#fcf0e8"
    };

    private final MoodboardRepository moodboardRepository;
    private final MoodboardContentService contentService;
    private final MoodboardNameService nameService;
    private final EntityReferences entityReferences;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public MoodboardSeedService(
            MoodboardRepository moodboardRepository,
            MoodboardContentService contentService,
            MoodboardNameService nameService,
            EntityReferences entityReferences,
            ObjectMapper objectMapper
    ) {
        this.moodboardRepository = moodboardRepository;
        this.contentService = contentService;
        this.nameService = nameService;
        this.entityReferences = entityReferences;
        this.objectMapper = objectMapper;
    }

    public int seedMoodboards(String ownerUsername, int count) {
        User owner = entityReferences.requireUser(ownerUsername);
        List<Moodboard> moodboards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            MoodboardContentDto content = buildRandomContent();
            Moodboard moodboard = new Moodboard(owner, contentService.serialize(content));
            moodboard.setName(nameService.normalizeForCreate(randomName(i)));
            moodboard.setPublic(random.nextBoolean());
            moodboards.add(moodboard);
        }
        moodboardRepository.saveAll(moodboards);
        return count;
    }

    private String randomName(int index) {
        String base = MOOD_NAMES[random.nextInt(MOOD_NAMES.length)];
        return base + " #" + (index + 1);
    }

    private MoodboardContentDto buildRandomContent() {
        MoodboardContentDto content = new MoodboardContentDto();
        content.setVersion(1);
        content.setCanvas(buildCanvas());

        List<MoodboardElementDto> elements = new ArrayList<>();
        elements.add(buildFabricElement());

        int textCount = 1 + random.nextInt(3);
        for (int i = 0; i < textCount; i++) {
            elements.add(buildTextElement(i));
        }
        content.setElements(elements);
        return content;
    }

    private ObjectNode buildCanvas() {
        ObjectNode canvas = objectMapper.createObjectNode();
        canvas.put("width", 900);
        canvas.put("height", 600);
        canvas.put("background", BACKGROUNDS[random.nextInt(BACKGROUNDS.length)]);
        return canvas;
    }

    private MoodboardElementDto buildFabricElement() {
        MoodboardElementDto element = new MoodboardElementDto();
        element.setId("main");
        element.setType("fabric");
        element.setFabricJson(buildFabricJson());
        return element;
    }

    private ObjectNode buildFabricJson() {
        ObjectNode fabricJson = objectMapper.createObjectNode();
        fabricJson.put("version", "7.4.0");
        fabricJson.put("background", BACKGROUNDS[random.nextInt(BACKGROUNDS.length)]);

        ArrayNode objects = objectMapper.createArrayNode();
        int shapeCount = 1 + random.nextInt(3);
        for (int i = 0; i < shapeCount; i++) {
            objects.add(buildRect(i));
        }
        fabricJson.set("objects", objects);
        return fabricJson;
    }

    private ObjectNode buildRect(int index) {
        ObjectNode rect = objectMapper.createObjectNode();
        rect.put("type", "Rect");
        rect.put("version", "7.4.0");
        rect.put("originX", "center");
        rect.put("originY", "center");
        rect.put("left", 80 + random.nextInt(700));
        rect.put("top", 60 + random.nextInt(450));
        rect.put("width", 80 + random.nextInt(200));
        rect.put("height", 60 + random.nextInt(160));
        rect.put("fill", FILLS[random.nextInt(FILLS.length)]);
        rect.put("stroke", COLORS[random.nextInt(COLORS.length)]);
        rect.put("strokeWidth", 1 + random.nextInt(3));
        rect.put("rx", 0);
        rect.put("ry", 0);
        rect.put("scaleX", 1);
        rect.put("scaleY", 1);
        rect.put("angle", random.nextInt(45));
        rect.put("opacity", 1);
        rect.put("visible", true);
        return rect;
    }

    private MoodboardElementDto buildTextElement(int index) {
        MoodboardElementDto element = new MoodboardElementDto();
        element.setId("text-" + UUID.randomUUID().toString().substring(0, 8));
        element.setType("text");
        element.setText(TEXT_SNIPPETS[random.nextInt(TEXT_SNIPPETS.length)]);
        element.setFontSize(16 + random.nextInt(20));
        element.setColor(COLORS[random.nextInt(COLORS.length)]);
        element.setX((double) (40 + random.nextInt(600)));
        element.setY((double) (40 + random.nextInt(400)));
        element.setWidth(120.0 + random.nextInt(280));
        element.setHeight(30.0 + random.nextInt(80));
        element.setZIndex(index + 1);
        element.setRotation((double) random.nextInt(20) - 10);
        return element;
    }
}
