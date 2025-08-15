package com.mk.springlangchain4j.web;

import com.mk.springlangchain4j.request.VectorDto;
import com.mk.springlangchain4j.response.ResponseResult;
import com.mk.springlangchain4j.response.VectorVo;
import com.mk.springlangchain4j.service.VectorAiService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping("/api/vector")
public class VectorController {
    @Autowired
    EmbeddingModel embeddingModel;
    @Autowired
    EmbeddingStore<TextSegment> milvusEmbeddingStore;
    @Autowired
    VectorAiService aiService;
    @PostMapping("chat")
    public Flux<String> ragChat(@RequestBody VectorDto vectorDto) {

        Embedding embedding = embeddingModel.embed(vectorDto.getQueryContent()).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .minScore(vectorDto.getThreshold() != null ? Double.parseDouble(vectorDto.getThreshold()) : 0.0)
                .maxResults(vectorDto.queryTotal != null ? vectorDto.queryTotal : 10)
                .build();
        EmbeddingSearchResult<TextSegment> embeddingSearchResult = milvusEmbeddingStore.search(searchRequest);
        StringBuilder builder = new StringBuilder();
        embeddingSearchResult.matches().forEach(itemVectorRes->{
            builder.append(itemVectorRes.embedded().text()).append("。");
        });
        //向量化文本
        vectorDto.ragContent=builder.toString();
        return aiService.streamingVectorChat(vectorDto.getMemoryId(), vectorDto.queryContent);
    }

    public <T> ResponseResult queryVector(String queryContent, Integer queryTotal, Double scoreThreshold, Function<EmbeddingSearchResult<TextSegment>, List<T>> func) {
        Embedding queryEmbedding = embeddingModel.embed(queryContent != null ? queryContent : "all").content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .maxResults(queryTotal != null ? queryTotal : 10)
                .queryEmbedding(queryEmbedding)
                .minScore(scoreThreshold != null ? scoreThreshold : 0.0)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = milvusEmbeddingStore.search(searchRequest);
        List<T> results = func.apply(searchResult);
        return ResponseResult.builder()
                .data(results)
                .message("success")
                .status(200)
                .build();
    }

    @GetMapping("search")
    public ResponseResult search(
            @RequestParam(defaultValue = "1") String type,
            @RequestParam(defaultValue = "0.65") String threshold,
            @RequestParam(defaultValue = "all") String queryContent,
            @RequestParam(defaultValue = "10") int queryTotal
    ) {
        List<VectorVo> list = new ArrayList<>();
        if ("1".equals(type)) {
            //1.查询全部向量
            return queryVector(queryContent, queryTotal, null, searchResult -> {
                ArrayList<VectorVo> vectorVos = new ArrayList<>();
                searchResult.matches().forEach(match -> {
                    VectorVo vectorVo = VectorVo.builder()
                            .vectorId(match.embeddingId())
                            .text(match.embedded().text())
                            .metadata(match.embedded().metadata().toString())
                            .vectorScore(String.valueOf(match.score()))
                            .build();
                    vectorVos.add(vectorVo);
                });
                return vectorVos;
            });

        } else if ("2".equals(type)) {
            //根据向量id查询
            return queryVector(queryContent, queryTotal, null, searchResult -> {
                ArrayList<VectorVo> vectorVos = new ArrayList<>();
                // 根据向量ID过滤结果
                searchResult.matches().stream()
                        .filter(match -> queryContent.equals(match.embeddingId()))
                        .limit(queryTotal)
                        .forEach(match -> {
                            VectorVo vectorVo = VectorVo.builder()
                                    .vectorId(match.embeddingId())
                                    .text(match.embedded().text())
                                    .metadata(match.embedded().metadata().toString())
                                    .vectorScore(String.valueOf(match.score()))
                                    .build();
                            vectorVos.add(vectorVo);
                        });
                return vectorVos;
            });
        } else if ("3".equals(type)) {
            return queryVector(queryContent, queryTotal, Double.parseDouble(threshold), searchResult -> {
                ArrayList<VectorVo> vectorVos = new ArrayList<>();
                searchResult.matches().forEach(match -> {
                    VectorVo vectorVo = VectorVo.builder()
                            .vectorId(match.embeddingId())
                            .text(match.embedded().text())
                            .metadata(match.embedded().metadata().toString())
                            .vectorScore(String.valueOf(match.score()))
                            .build();
                    vectorVos.add(vectorVo);
                });
                return vectorVos;
            });
        }

        return ResponseResult.builder()
                .data(list)
                .message("success")
                .status(200)
                .build();
    }

    @PostMapping("add")
    public ResponseResult add(@RequestBody VectorDto vectorDto) {
        Metadata metadata = Metadata.from(
                Map.of(
                        "text", vectorDto.queryContent,
                        "date", new Date()
                )
        );
        TextSegment textSegment = TextSegment.from(vectorDto.queryContent, metadata);
        Embedding embedding = embeddingModel.embed(textSegment).content();
        milvusEmbeddingStore.add(embedding, textSegment);
        return ResponseResult.builder()
                .data("success")
                .message("success")
                .status(200)
                .build();
    }

    /**
     * 传统全文搜索 - 基于关键词匹配
     * 注意：此实现为模拟全文搜索，实际项目中建议使用专门的全文搜索引擎如Elasticsearch
     */
    @GetMapping("fullTextSearch")
    public ResponseResult fullTextSearch(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "10") int maxResults
    ) {
        List<VectorVo> list = new ArrayList<>();

        // 获取所有文档进行关键词匹配（实际应用中应该在数据库层面进行优化）
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .maxResults(1000) // 获取更多结果用于关键词过滤
                .queryEmbedding(embeddingModel.embed("dummy").content()) // 临时向量，仅用于获取所有数据
                .minScore(0.0) // 设置最低分数为0以获取所有结果
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = milvusEmbeddingStore.search(searchRequest);

        // 关键词匹配过滤
        String[] keywordArray = keywords.toLowerCase().split("\\s+");
        searchResult.matches().stream()
                .filter(match -> {
                    String text = match.embedded().text().toLowerCase();
                    return java.util.Arrays.stream(keywordArray)
                            .anyMatch(text::contains);
                })
                .limit(maxResults)
                .forEach(match -> {
                    VectorVo vectorVo = VectorVo.builder()
                            .vectorId(match.embeddingId())
                            .text(match.embedded().text())
                            .metadata(match.embedded().metadata().toString())
                            .vectorScore(String.valueOf(match.score()))
                            .build();
                    list.add(vectorVo);
                });

        return ResponseResult.builder()
                .data(list)
                .message("Full text search completed")
                .status(200)
                .build();
    }
    /**
     * 检索方式：语义检索，关键词检索，混合检索
     */
    /**
     * 混合搜索 - 结合向量相似度搜索和关键词匹配
     */
    @GetMapping("hybridSearch")
    public ResponseResult hybridSearch(
            @RequestParam String queryContent,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "10") int maxResults,
            @RequestParam(defaultValue = "0.5") double vectorWeight,
            @RequestParam(defaultValue = "0.5") double keywordWeight
    ) {
        List<VectorVo> list = new ArrayList<>();

        // 1. 向量相似度搜索
        Embedding queryEmbedding = embeddingModel.embed(queryContent).content();
        EmbeddingSearchRequest vectorSearchRequest = EmbeddingSearchRequest.builder()
                .maxResults(maxResults * 2) // 获取更多结果用于混合排序
                .queryEmbedding(queryEmbedding)
                .build();

        EmbeddingSearchResult<TextSegment> vectorResults = milvusEmbeddingStore.search(vectorSearchRequest);

        // 2. 关键词匹配和混合评分
        String[] keywordArray = keywords != null ? keywords.toLowerCase().split("\\s+") : new String[0];

        vectorResults.matches().stream()
                .map(match -> {
                    double vectorScore = match.score();
                    double keywordScore = 0.0;

                    // 计算关键词匹配分数
                    if (keywordArray.length > 0) {
                        String text = match.embedded().text().toLowerCase();
                        long matchCount = java.util.Arrays.stream(keywordArray)
                                .mapToLong(keyword -> text.split(keyword, -1).length - 1)
                                .sum();
                        keywordScore = Math.min(1.0, matchCount / (double) keywordArray.length);
                    }

                    // 混合评分
                    double hybridScore = vectorScore * vectorWeight + keywordScore * keywordWeight;

                    VectorVo vectorVo = VectorVo.builder()
                            .vectorId(match.embeddingId())
                            .text(match.embedded().text())
                            .metadata(match.embedded().metadata().toString())
                            .vectorScore(String.format("%.4f (V:%.4f, K:%.4f)", hybridScore, vectorScore, keywordScore))
                            .build();

                    return new AbstractMap.SimpleEntry<>(hybridScore, vectorVo);
                })
                .sorted((a, b) -> Double.compare(b.getKey(), a.getKey())) // 按混合分数降序排序
                .limit(maxResults)
                .forEach(entry -> list.add(entry.getValue()));

        return ResponseResult.builder()
                .data(list)
                .message("Hybrid search completed")
                .status(200)
                .build();
    }

    /**
     * 基于元数据字段查询
     */
    //@GetMapping("searchByMetadata")
    //public ResponseResult searchByMetadata(
    //        @RequestParam String metadataKey,
    //        @RequestParam String metadataValue,
    //        @RequestParam(defaultValue = "10") int maxResults,
    //        @RequestParam(defaultValue = "exact") String matchType // exact, contains
    //) {
    //    List<VectorVo> list = new ArrayList<>();
    //
    //    // 构建元数据过滤器
    //    Filter filter;
    //    if ("contains".equals(matchType)) {
    //        // 对于包含匹配，我们需要获取所有数据然后过滤（实际应用中应在数据库层优化）
    //        filter = null; // 暂时不使用过滤器，在后续代码中手动过滤
    //    } else {
    //        // 精确匹配
    //        filter = new IsEqualTo(metadataKey, metadataValue);
    //    }
    //
    //    EmbeddingSearchRequest.Builder requestBuilder = EmbeddingSearchRequest.builder()
    //            .maxResults("contains".equals(matchType) ? 1000 : maxResults)
    //            .queryEmbedding(embeddingModel.embed("dummy").content()) // 临时向量
    //            .minScore(0.0);
    //
    //    if (filter != null) {
    //        requestBuilder.filter(filter);
    //    }
    //
    //    EmbeddingSearchRequest searchRequest = requestBuilder.build();
    //    EmbeddingSearchResult<TextSegment> searchResult = milvusEmbeddingStore.search(searchRequest);
    //
    //    searchResult.matches().stream()
    //            .filter(match -> {
    //                if ("contains".equals(matchType)) {
    //                    // 手动进行包含匹配过滤
    //                    String actualValue = match.embedded().metadata().getString(metadataKey);
    //                    return actualValue != null && actualValue.toLowerCase().contains(metadataValue.toLowerCase());
    //                }
    //                return true; // 精确匹配已通过filter处理
    //            })
    //            .limit(maxResults)
    //            .forEach(match -> {
    //                VectorVo vectorVo = VectorVo.builder()
    //                        .vectorId(match.embeddingId())
    //                        .text(match.embedded().text())
    //                        .metadata(match.embedded().metadata().toString())
    //                        .vectorScore(String.valueOf(match.score()))
    //                        .build();
    //                list.add(vectorVo);
    //            });
    //
    //    return ResponseResult.builder()
    //            .data(list)
    //            .message("Metadata search completed")
    //            .status(200)
    //            .build();
    //}

    /**
     * 知识库文档上传接口
     * 支持多种文档格式：TXT、MD、PDF、Word(DOC/DOCX)等
     *
     * @param file 上传的文档文件
     * @return 处理结果，包含成功处理的段落数量等信息
     */
    @PostMapping("addDocument")
    public ResponseResult addDocument(MultipartFile file) {
        // 1. 基础参数验证
        if (file.isEmpty()) {
            return ResponseResult.builder()
                    .message("文件不能为空")
                    .status(400)
                    .build();
        }

        String fileName = file.getOriginalFilename();
        if (fileName.trim().isEmpty()) {
            return ResponseResult.builder()
                    .message("文件名不能为空")
                    .status(400)
                    .build();
        }
        try {
            // 2. 根据文件类型选择合适的文档解析器
            Document document = parseDocumentByType(file, fileName);

            if (document == null || document.text().trim().isEmpty()) {
                return ResponseResult.builder()
                        .message("文档内容为空或解析失败")
                        .status(400)
                        .build();
            }

            // 3. 添加文档元数据信息
            Metadata metadata = createDocumentMetadata(file, fileName);
            document = Document.from(document.text(), metadata);

            // 4. 智能分割文档为小段落
            List<TextSegment> segments = splitDocument(document);

            // 5. 批量处理段落：生成向量并存储到知识库
            int successCount = processSegmentsWithRateLimit(segments);

            // 6. 返回处理结果
            return ResponseResult.builder()
                    .data(Map.of(
                            "totalSegments", segments.size(),
                            "successCount", successCount,
                            "fileName", fileName,
                            "fileSize", formatFileSize(file.getSize()),
                            "documentType", getDocumentType(fileName)
                    ))
                    .message(String.format("文档上传成功！共处理 %d 个段落，成功存储 %d 个段落到知识库",
                            segments.size(), successCount))
                    .status(200)
                    .build();

        } catch (Exception e) {
            System.err.println("文档处理异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseResult.builder()
                    .message("文档处理失败: " + e.getMessage())
                    .status(500)
                    .build();
        }
    }

    /**
     * 根据文件类型选择合适的文档解析器
     *
     * @param file     上传的文件
     * @param fileName 文件名
     * @return 解析后的文档对象
     * @throws IOException 文件读取异常
     */
    private Document parseDocumentByType(MultipartFile file, String fileName) throws IOException {
        String fileExtension = getFileExtension(fileName).toLowerCase();

        switch (fileExtension) {
            case "pdf":
                // PDF文档解析 - 使用Apache PDFBox
                System.out.println("正在解析PDF文档: " + fileName);
                ApachePdfBoxDocumentParser pdfParser = new ApachePdfBoxDocumentParser();
                return pdfParser.parse(file.getInputStream());

            case "doc":
            case "docx":
            case "ppt":
            case "pptx":
            case "xls":
            case "xlsx":
                // Microsoft Office文档解析 - 使用Apache POI
                System.out.println("正在解析Office文档: " + fileName + " (类型: " + fileExtension.toUpperCase() + ")");
                ApachePoiDocumentParser poiParser = new ApachePoiDocumentParser();
                return poiParser.parse(file.getInputStream());

            case "txt":
            case "md":
            case "markdown":
            case "log":
            case "csv":
            case "json":
            case "xml":
            case "html":
            case "htm":
            default:
                // 纯文本文档解析 - 支持TXT、MD、HTML等文本格式
                System.out.println("正在解析文本文档: " + fileName + " (类型: " + fileExtension.toUpperCase() + ")");
                TextDocumentParser textParser = new TextDocumentParser();
                return textParser.parse(file.getInputStream());
        }
    }

    /**
     * 创建文档元数据
     *
     * @param file     上传的文件
     * @param fileName 文件名
     * @return 元数据对象
     */
    private Metadata createDocumentMetadata(MultipartFile file, String fileName) {
        return Metadata.from(
                Map.of(
                        "fileName", fileName,
                        "fileSize", String.valueOf(file.getSize()),
                        "fileSizeFormatted", formatFileSize(file.getSize()),
                        "uploadTime", String.valueOf(System.currentTimeMillis()),
                        "uploadDate", new Date().toString(),
                        "contentType", file.getContentType() != null ? file.getContentType() : "text/plain",
                        "documentType", getDocumentType(fileName),
                        "fileExtension", getFileExtension(fileName)
                )
        );
    }

    /**
     * 智能分割文档为合适的段落
     *
     * @param document 文档对象
     * @return 分割后的文本段落列表
     */
    private List<TextSegment> splitDocument(Document document) {
        // 根据文档长度动态调整分割参数
        int documentLength = document.text().length();
        int maxSegmentSize;
        int overlapSize;

        if (documentLength < 2000) {
            // 短文档：较小的段落，减少分割
            maxSegmentSize = 300;
            overlapSize = 30;
        } else if (documentLength < 10000) {
            // 中等文档：标准分割
            maxSegmentSize = 500;
            overlapSize = 50;
        } else {
            // 长文档：较大的段落，保持上下文连贯性
            maxSegmentSize = 800;
            overlapSize = 80;
        }

        System.out.println(String.format("文档长度: %d 字符，使用分割参数: 最大段落长度=%d, 重叠长度=%d",
                documentLength, maxSegmentSize, overlapSize));

        return DocumentSplitters.recursive(
                maxSegmentSize,  // 最大段落长度
                overlapSize      // 段落重叠长度，保持上下文连贯性
        ).split(document);
    }

    /**
     * 批量处理文档段落，包含速率限制和重试机制
     *
     * @param segments 文档段落列表
     * @return 成功处理的段落数量
     */
    private int processSegmentsWithRateLimit(List<TextSegment> segments) {
        int successCount = 0;
        int totalSegments = segments.size();

        System.out.println("开始处理 " + totalSegments + " 个文档段落...");

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            boolean processed = false;
            int retryCount = 0;
            int maxRetries = 3;

            // 重试机制处理单个段落
            while (!processed && retryCount < maxRetries) {
                try {
                    // 生成向量嵌入
                    Embedding embedding = embeddingModel.embed(segment).content();
                    // 存储到Milvus向量数据库
                    milvusEmbeddingStore.add(embedding, segment);

                    successCount++;
                    processed = true;

                    // 进度提示
                    if ((i + 1) % 10 == 0 || (i + 1) == totalSegments) {
                        System.out.println(String.format("已处理: %d/%d 个段落 (%.1f%%)",
                                i + 1, totalSegments, (i + 1) * 100.0 / totalSegments));
                    }

                } catch (Exception e) {
                    retryCount++;
                    System.err.println(String.format("处理第 %d 个段落失败 (重试 %d/%d): %s",
                            i + 1, retryCount, maxRetries, e.getMessage()));

                    if (retryCount < maxRetries) {
                        try {
                            // 指数退避重试策略
                            Thread.sleep(retryCount * 5000L); // 5秒、10秒、15秒
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            // 速率控制：避免触发Milvus限流
            try {
                Thread.sleep(100); // 每个段落间延迟100毫秒

                // 每处理10个段落后额外延迟
                if ((i + 1) % 10 == 0) {
                    Thread.sleep(2000); // 额外延迟2秒
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(String.format("文档处理完成！总段落: %d, 成功: %d, 失败: %d",
                totalSegments, successCount, totalSegments - successCount));

        return successCount;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（小写）
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文档类型描述
     *
     * @param fileName 文件名
     * @return 文档类型描述
     */
    private String getDocumentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "pdf":
                return "PDF文档";
            case "doc":
            case "docx":
                return "Word文档";
            case "ppt":
            case "pptx":
                return "PowerPoint演示文稿";
            case "xls":
            case "xlsx":
                return "Excel表格";
            case "txt":
                return "纯文本文档";
            case "md":
            case "markdown":
                return "Markdown文档";
            case "html":
            case "htm":
                return "HTML网页";
            case "json":
                return "JSON数据";
            case "xml":
                return "XML文档";
            case "csv":
                return "CSV表格";
            case "log":
                return "日志文件";
            default:
                return "文本文档";
        }
    }

    /**
     * 格式化文件大小显示
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
