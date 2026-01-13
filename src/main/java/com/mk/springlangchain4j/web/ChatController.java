package com.mk.springlangchain4j.web;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mk.springlangchain4j.entity.ChatConversationEntity;
import com.mk.springlangchain4j.entity.ChatMessageEntity;
import com.mk.springlangchain4j.entity.Person;
import com.mk.springlangchain4j.enums.MessageRole;
import com.mk.springlangchain4j.response.ResponseResult;
import com.mk.springlangchain4j.service.ChatConversationService;
import com.mk.springlangchain4j.service.ChatMessageService;
import com.mk.springlangchain4j.service.CustomAiService;
import com.mk.springlangchain4j.service.StructAiService;
import com.mk.springlangchain4j.store.message.MessageHistoryRedisStoreProvider;
import com.mk.springlangchain4j.vo.ChatConversationVo;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@CrossOrigin
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    ChatModel chatModel;
    @Autowired
    StreamingChatModel streamingChatModel;
    @Autowired
    CustomAiService aiService;
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    ChatConversationService chatConversationService;
    @Autowired
    ChatMessageService chatMessageService;
    String prompt = """
            帮我将这些用户解析出来，解析成json格式，里面有一些数据是1 2 3 这种脏数据就不要添加了
            最终生成格式例如:{"name":"姓名",phone":"手机号","age":"年纪不要带岁字,如果是小孩儿比如三岁半就输出3.5","gender":"男或女，不清楚就未知","address":"如果出现地址"},{"name":"姓名",phone":"手机号","age":"年纪不要带岁字,如果是小孩儿比如三岁半就输出3.5","gender":"男或女，不清楚就未知","address":"如果出现地址"}
            """;


    @GetMapping("chat")
    public void getData() {
        StringBuilder builder = new StringBuilder();
        List<String> list = readPatientDataXls();
        // 将 list 集合中的数据写入缓冲区（逐行写入并追加换行符），最终保存到 D 盘 txt 文件
        if (list != null && !list.isEmpty()) {
            try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(
                    java.nio.file.Paths.get("D:\\patient_data.txt"),
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            )) {
                for (String line : list) {
                    writer.write(line);
                    writer.write("\n"); // 每写入一行后追加 \n
                }
                writer.flush();
                System.out.println("病人数据已写入：D:\\patient_data.txt");
            } catch (Exception e) {
                System.err.println("写入病人数据到 txt 文件失败：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("病人数据为空，未写入 txt 文件。");
        }

        //SystemMessage systemMessage = SystemMessage.from(prompt);
        //List<List<String>> listList = partition(list, 50);
        //for (List<String> strings : listList) {
        //    UserMessage userMessage = UserMessage.from(strings.toString());
        //    String content = chatModel.chat(systemMessage, userMessage).aiMessage().text();
        //    builder.append(content).append("\n");
        //    System.out.println(content);
        //}
        //
        //// 将 builder 中的内容写入到 D 盘 JSON 文件（每行作为一个数组元素），便于后续解析
        //try {
        //    String allContent = builder.toString().trim();
        //    String[] lines = allContent.split("\\R"); // 按任意换行符拆分
        //    List<String> outputs = new ArrayList<>();
        //    for (String line : lines) {
        //        String trimmed = line.trim();
        //        if (!trimmed.isEmpty()) {
        //            outputs.add(trimmed);
        //        }
        //    }
        //
        //    ObjectMapper mapper = new ObjectMapper();
        //    File outFile = new File("D:~\\chat_output.json".replace("~", "")); // 兼容路径字符串
        //    mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, outputs);
        //    System.out.println("JSON写入完成，路径：" + outFile.getAbsolutePath());
        //} catch (Exception e) {
        //    // 写入失败时打印异常，便于排查
        //    System.err.println("写入JSON文件失败：" + e.getMessage());
        //    e.printStackTrace();
        //}
    }

    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null || list.isEmpty() || size <= 0) {
            return new ArrayList<>();
        }

        int numberOfGroups = (int) Math.ceil((double) list.size() / size);

        return IntStream.range(0, numberOfGroups)
                .mapToObj(i -> list.subList(
                        Math.min(i * size, list.size()),
                        Math.min((i + 1) * size, list.size())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 读取resources目录下的病人数据Excel文件
     * 读取每一行的第二列字符串数据并返回List集合
     *
     * @return 包含所有行第二列数据的List集合
     * @throws IOException 文件读取异常
     */
    public List<String> readPatientDataExcel() throws IOException {
        // 获取资源文件路径
        ClassPathResource resource = new ClassPathResource("病人数据.xls");

        // 创建结果集合
        List<String> result = new ArrayList<>();

        try {
            // 使用EasyExcel读取Excel文件，不指定实体类，直接读取Map数据
            EasyExcel.read(resource.getFile(), new ReadListener<Map<Integer, String>>() {

                private int totalRows = 0;

                /**
                 * 每读取一行数据时调用
                 */
                @Override
                public void invoke(Map<Integer, String> rowData, AnalysisContext analysisContext) {
                    totalRows++;

                    // 跳过表头行
                    if (totalRows == 1) {
                        return;
                    }

                    // 只读取第二列数据（索引为1，因为EasyExcel从0开始）
                    String secondColumnValue = rowData.get(1);
                    if (secondColumnValue != null && secondColumnValue.startsWith("客户名称")) {
                        result.add(secondColumnValue);
                    }
                }

                /**
                 * 所有数据读取完成时调用
                 */
                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    System.out.println("Excel文件读取完成，总行数: " + (totalRows - 1));
                    System.out.println("成功读取第二列数据: " + result.size() + " 条");
                }

            }).sheet().doRead();

        } catch (Exception e) {
            throw new IOException("读取Excel文件失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 示例用法：读取病人数据Excel文件并返回第二列数据
     */
    public List<String> readPatientDataXls() {
        try {
            List<String> secondColumnData = readPatientDataExcel();

            System.out.println("总共读取到 " + secondColumnData.size() + " 条第二列数据");

            // 这里可以添加具体的数据处理逻辑
            for (String value : secondColumnData) {
                // 处理每一行的第二列数据
                System.out.println("第二列数据: " + value);
            }
            return secondColumnData;
        } catch (IOException e) {
            System.err.println("读取Excel文件失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("structured")
    public ResponseResult struct(@RequestParam String message) {
        StructAiService structAiService = AiServices.builder(StructAiService.class)
                .chatModel(chatModel)
                .build();
        Person person = structAiService.parsePerson(message);
        return ResponseResult.builder()
                .data(person)
                .message("success")
                .status(200)
                .build();
    }


    @GetMapping
    public ResponseResult chat(String message) {
        return ResponseResult.builder()
                .data(aiService.chat(message))
                .message("success")
                .status(200)
                .build();
    }

    @PostMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingChat(String message) {
        return aiService.streamingChat(message);
    }

    @PostMapping(value = "streamHis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingChatWithHistory(
            @RequestParam String memoryId,
            @RequestParam String sessionId,
            @RequestParam String userMessage) {
        return aiService.streamingChat(memoryId + ":" + sessionId, userMessage);
    }

    @Transactional
    @PostMapping("save")
    public ResponseResult save(@RequestParam String memoryId, @RequestParam String sessionId) {
        List<String> messageRecord = redis.opsForList().range("message_record:" + memoryId + ":" + sessionId, 0, -1);
        List<ChatMessage> chatMessages = messageRecord.stream().map(ChatMessageDeserializer::messageFromJson).toList();
        List<ChatMessageEntity> chatMessageRecords = new ArrayList<>();
        QueryWrapper<ChatConversationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", memoryId);
        queryWrapper.eq("session_id", sessionId);
        ChatConversationEntity exist = chatConversationService.getOne(queryWrapper);
        ChatConversationEntity chatConversation = null;
        if (exist == null) {
            chatConversation = ChatConversationEntity.builder()
                    .userId(memoryId)
                    .sessionId(sessionId)
                    .title(((TextContent) ((UserMessage) chatMessages.get(0)).contents().get(0)).text())
                    .startTime(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            chatConversationService.save(chatConversation);
        } else {
            chatConversation = exist;
        }
        for (ChatMessage itemRecord : chatMessages) {
            if (itemRecord.type() == ChatMessageType.AI) {
                // 处理AI消息
                ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                        .content(((AiMessage) itemRecord).text())
                        .conversationId(chatConversation.getId())
                        .role(MessageRole.AI)
                        .messageTime(LocalDateTime.now())
                        .build();
                chatMessageRecords.add(chatMessage);
            } else if (itemRecord.type() == ChatMessageType.USER) {
                // 处理用户消息
                ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                        .conversationId(chatConversation.getId())
                        .content(((TextContent) ((UserMessage) itemRecord).contents().get(0)).text())
                        .role(MessageRole.USER)
                        .messageTime(LocalDateTime.now())
                        .build();
                chatMessageRecords.add(chatMessage);
            }
        }
        QueryWrapper<ChatMessageEntity> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("conversation_id", chatConversation.getId());
        chatMessageService.getBaseMapper().delete(deleteWrapper);
        chatMessageService.saveBatch(chatMessageRecords);
        return ResponseResult.builder()
                .data("success")
                .message("success")
                .status(200)
                .build();
    }

    @GetMapping("getHistoryRecords")
    public ResponseResult getHistoryRecords(@RequestParam String memoryId) {
        QueryWrapper<ChatConversationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", memoryId);
        wrapper.orderByDesc("session_id");
        List<ChatConversationEntity> conversationEntityList = chatConversationService.getBaseMapper().selectList(wrapper);
        List<ChatConversationVo> list = new ArrayList<>();
        conversationEntityList.forEach(itemHistoryConversation -> {
            QueryWrapper<ChatMessageEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("conversation_id", itemHistoryConversation.getId());
            List<ChatMessageEntity> chatMessageRecords = chatMessageService.getBaseMapper().selectList(queryWrapper);
            ChatConversationVo chatConversationVo = BeanUtil.toBean(itemHistoryConversation, ChatConversationVo.class);
            chatConversationVo.setTotal(chatMessageRecords.size());
            list.add(chatConversationVo);
        });
        return ResponseResult.builder()
                .data(list)
                .message("success")
                .status(200)
                .build();
    }

    @Transactional
    @GetMapping("/conversation/detail")
    public ResponseResult getConversationDetail(@RequestParam("conversationId") Long conversationId) {
        QueryWrapper<ChatMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        List<ChatMessageEntity> chatMessageEntities = chatMessageService.getBaseMapper().selectList(queryWrapper);

        QueryWrapper<ChatConversationEntity> queryConversationWrapper = new QueryWrapper<>();
        queryConversationWrapper.eq("id", conversationId);
        ChatConversationEntity conversation = chatConversationService.getOne(queryConversationWrapper);
        redis.delete(
                MessageHistoryRedisStoreProvider.MESSAGE_HISTORY_PREFIX +
                        conversation.getUserId() +
                        ":" +
                        conversation.getSessionId()
        );
        chatMessageEntities.forEach(itemChatMessageRecord -> {
            ChatMessage chatMessage = null;
            if ("USER".equalsIgnoreCase(itemChatMessageRecord.getRole().getCode())) {
                chatMessage = UserMessage.builder()
                        .contents(List.of(new TextContent(itemChatMessageRecord.getContent())))
                        .build();
            } else if ("AI".equalsIgnoreCase(itemChatMessageRecord.getRole().getCode())) {
                chatMessage = AiMessage.builder()
                        .text(itemChatMessageRecord.getContent())
                        .build();
            }
            redis.opsForList().rightPush(
                    MessageHistoryRedisStoreProvider.MESSAGE_HISTORY_PREFIX +
                            conversation.getUserId() +
                            ":" +
                            conversation.getSessionId(),
                    ChatMessageSerializer.messageToJson(chatMessage)
            );
        });
        return ResponseResult.builder()
                .message("success")
                .status(200)
                .data(chatMessageEntities)
                .build();
    }


}
