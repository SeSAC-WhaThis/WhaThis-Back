package com.example.whathis.aiSupport.inquiry.tools;

import com.example.whathis.aiSupport.inquiry.dto.InquiryDTOs;
import com.example.whathis.aiSupport.inquiry.service.InquiryFundingService;
import com.example.whathis.category.dto.response.CategoryResponse;
import com.example.whathis.category.service.CategoryService;
import com.example.whathis.common.exception.BusinessException;
import com.example.whathis.order.dto.response.OrderCreateResponse;
import com.example.whathis.order.service.OrderService;
import com.example.whathis.product.dto.response.ProductDetailResponse;
import com.example.whathis.product.dto.response.ProductResponse;
import com.example.whathis.product.service.ProductService;
import com.example.whathis.product.dto.response.UserProfileResponse;
import com.example.whathis.review.dto.response.ReviewResponse;
import com.example.whathis.review.service.ReviewService;
import com.example.whathis.user.entity.User;
import com.example.whathis.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InquiryTools {

    private static final String UNKNOWN = "(알 수 없음)";
    private static final String FRONT_BASE_URL = "http://localhost:5173";
    private static final Pattern KOREAN_DATE_PATTERN = Pattern.compile(
        "(?<y>\\d{4})\\s*년\\s*(?<m>\\d{1,2})\\s*월\\s*(?<d>\\d{1,2})\\s*일"
    );

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final OrderService orderService;
    private final InquiryFundingService inquiryFundingService;

    // ------ Product 관련 tools
    @Tool(description = "현재 등록된 모든 상품목록을 조회합니다. 상품명, 가격, 펀딩 현황 등을 확인할 수 있습니다.")
    public String getAllProducts() {
        List<ProductResponse> products = productService.findAll(null, null);

        if (products.isEmpty()) {
            return "현재 등록된 상품이 없습니다.";
        }

        return products.stream()
            .map(this::formatProductInfo)
            .collect(Collectors.joining("\n\n"));
    }

    @Tool(description = "특정 상품의 상세 정보를 조회합니다. 상품 ID를 입력받아 해당 상품의 상세 정보를 반환합니다.")
    public String getProductById(
        @ToolParam(description = "조회할 상품의 ID 번호") Long productId) {
        try {
            ProductDetailResponse detail = productService.getDetail(productId, null);
            return formatProductDetailInfo(detail);
        } catch (BusinessException e) {
            return e.getMessage();
        }
    }

    @Tool(description = "상품을 키워드로 검색합니다. 상품명이나 설명에 해당 키워드가 포함된 상품을 찾습니다.")
    public String searchProducts(
        @ToolParam(description = "검색할 키워드 (상품명, 설명에서 검색)") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "검색 키워드가 필요합니다.";
        }
        List<ProductResponse> products = productService.findAll(null, null);

        List<ProductResponse> filtered = products.stream()
            .filter(p -> (p.getTitle() != null && p.getTitle().contains(keyword)) ||
                (p.getDescription() != null && p.getDescription().contains(keyword)))
            .toList();

        if (filtered.isEmpty()) {
            return "'" + keyword + "'로 검색된 상품이 없습니다.";
        }

        return "검색 결과 (" + filtered.size() + "개):\n\n" +
            filtered.stream()
                .map(this::formatProductInfo)
                .collect(Collectors.joining("\n\n"));
    }

    @Tool(description = "특정 카테고리에 속한 상품 목록을 조회합니다. 카테고리명을 입력받아 해당 카테고리의 상품들을 반환합니다.")
    public String getProductsByCategory(
        @ToolParam(description = "조회할 카테고리명 (예: 전자제품, 패션, 뷰티, 푸드 등)") String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return "카테고리명이 필요합니다.";
        }
        Long categoryId = resolveCategoryIdOrNull(null, categoryName);
        if (categoryId == null) {
            return "카테고리를 찾을 수 없습니다: '" + categoryName + "'.\n" +
                "가능한 카테고리: " + categoriesHintText();
        }

        List<ProductResponse> filtered = productService.findAll(categoryId, null);

        if (filtered.isEmpty()) {
            return "'" + categoryName + "' 카테고리에 등록된 상품이 없습니다.";
        }

        return "'" + categoryName + "' 카테고리 상품 (" + filtered.size() + "개):\n\n" +
            filtered.stream()
                .map(this::formatProductInfo)
                .collect(Collectors.joining("\n\n"));
    }

    // ------ Review 관련 tools
    @Tool(description = "특정 상품의 리뷰 목록을 조회합니다. 상품 ID를 입력받아 최신 리뷰들을 요약해 반환합니다.")
    public String getProductReviews(InquiryDTOs.ProductReviewsRequest request) {
        if (request == null || request.productId() == null) {
            return "상품 ID가 필요합니다.";
        }

        try {
            List<ReviewResponse> reviews = reviewService.findAll(request.productId());
            if (reviews.isEmpty()) {
                return "해당 상품에 아직 리뷰가 없습니다.";
            }

            return "리뷰 (" + reviews.size() + "개):\n\n" + reviews.stream()
                .map(this::formatReviewInfo)
                .collect(Collectors.joining("\n\n"));
        } catch (BusinessException e) {
            return e.getMessage();
        }
    }

    // ------ Seller 관련 tools
    @Tool(description = "판매자(유저) 프로필을 조회합니다. 판매자 ID를 입력받아 평점/팔로워/판매 중 상품 요약을 반환합니다.")
    public String getSellerProfile(InquiryDTOs.SellerProfileRequest request, ToolContext toolContext) {
        if (request == null || request.sellerId() == null) {
            return "판매자 ID가 필요합니다.";
        }

        try {
            User currentUser = resolveAuthenticatedUserOrNull(toolContext);
            UserProfileResponse profile = userService.getUserProfile(request.sellerId(), currentUser);
            return formatSellerProfile(profile);
        } catch (BusinessException e) {
            return e.getMessage();
        }
    }

    //  ----- Funding 관련 tools
    @Tool(description = "펀딩 프로젝트(상품)를 오픈합니다. 로그인한 판매자만 가능하며, 제목/설명/가격/목표금액/카테고리/기간/이미지URL/재고가 필요합니다.")
    public String openFundingProject(InquiryDTOs.FundProductRequest request, ToolContext toolContext) {
        User currentUser = resolveAuthenticatedUserOrNull(toolContext);
        if (currentUser == null) {
            return "로그인이 필요합니다. 로그인 후 다시 시도해 주세요.";
        }
        if (request == null) {
            return "요청 값이 비어있습니다.";
        }

        Long categoryId = resolveCategoryIdOrNull(request.categoryId(), request.categoryName());
        if (categoryId == null) {
            return "카테고리명을 정확히 입력해 주세요. (예: 전자제품)\n" +
                "가능한 카테고리: " + categoriesHintText();
        }

        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = parseFlexibleDateTime(request.startDate(), LocalTime.of(9, 0, 0));
            endDate = parseFlexibleDateTime(request.endDate(), LocalTime.of(23, 59, 59));
        } catch (Exception e) {
            return "오류: 날짜 형식이 올바르지 않습니다. 예: 2026년 1월 12일 또는 오늘";
        }

        try {
            ProductDetailResponse created = inquiryFundingService.createFundingProduct(
                currentUser,
                request.brn(),
                request.title(),
                request.description(),
                request.price(),
                request.goalAmount(),
                categoryId,
                startDate,
                endDate,
                request.thumbnailImageUrl(),
                request.storyImageUrl(),
                request.inventory());

            return "프로젝트가 오픈되었습니다.\n\n" + formatProductDetailInfo(created);
        } catch (BusinessException e) {
            return e.getMessage();
        }
    }


    // ------ 사용자에게 보여주기 위한 정보 포메팅 관련 메소드

    private String formatProductInfo(ProductResponse product) {
        String sellerName = displayName(
            product.getSeller() != null ? product.getSeller().getNickname() : null,
            product.getSeller() != null ? product.getSeller().getName() : null
        );
        Long sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        Long productId = product.getId();

        return String.format(
            "[%s]\n" +
                "- 가격: %,d원\n" +
                "- 펀딩 목표: %,d원 (현재 %,d원, 달성률 %.1f%%)\n" +
                "- 참여자 수: %d명\n" +
                "- 남은 일수: %d일\n" +
                "- 카테고리: %s\n" +
                "- 상품 페이지: %s\n" +
                "- 판매자: %s\n" +
                "- 판매자 프로필: %s",
            product.getTitle(),
            product.getPrice() != null ? product.getPrice().intValue() : 0,
            product.getGoalAmount() != null ? product.getGoalAmount().intValue() : 0,
            product.getCurrentAmount() != null ? product.getCurrentAmount().intValue() : 0,
            product.getAchievementRate() != null ? product.getAchievementRate() : 0.0,
            product.getBuyerCount() != null ? product.getBuyerCount() : 0,
            product.getDaysLeft() != null ? product.getDaysLeft() : 0,
            product.getCategory() != null ? product.getCategory().getName() : "미분류",
            productUrl(productId),
            sellerName,
            userProfileUrl(sellerId));
    }


    private String formatProductDetailInfo(ProductDetailResponse product) {
        String sellerName = displayName(
            product.getSeller() != null ? product.getSeller().getNickname() : null,
            product.getSeller() != null ? product.getSeller().getName() : null
        );
        Long sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        Long productId = product.getId();

        return String.format(
            "상품 상세 정보\n" +
                "================\n" +
                "상품명: %s\n" +
                "설명: %s\n" +
                "가격: %,d원\n" +
                "재고: %d개\n" +
                "펀딩 목표금액: %,d원\n" +
                "현재 모금액: %,d원\n" +
                "달성률: %.1f%%\n" +
                "참여자 수: %d명\n" +
                "시작일: %s\n" +
                "종료일: %s\n" +
                "남은 일수: %d일\n" +
                "카테고리: %s\n" +
                "판매자: %s\n" +
                "판매자 프로필: %s\n" +
                "상품 페이지: %s\n" +
                "썸네일: %s\n" +
                "스토리 이미지: %s",
            product.getTitle(),
            product.getDescription() != null ? product.getDescription() : "(설명 없음)",
            product.getPrice() != null ? product.getPrice().intValue() : 0,
            product.getInventory() != null ? product.getInventory() : 0,
            product.getGoalAmount() != null ? product.getGoalAmount().intValue() : 0,
            product.getCurrentAmount() != null ? product.getCurrentAmount().intValue() : 0,
            product.getAchievementRate() != null ? product.getAchievementRate() : 0.0,
            product.getBuyerCount() != null ? product.getBuyerCount() : 0,
            product.getStartDate() != null ? product.getStartDate().toLocalDate() : "(미정)",
            product.getEndDate() != null ? product.getEndDate().toLocalDate() : "(미정)",
            product.getDaysLeft() != null ? product.getDaysLeft() : 0,
            product.getCategory() != null ? product.getCategory().getName() : "미분류",
            sellerName,
            userProfileUrl(sellerId),
            productUrl(productId),
            product.getThumbnailImageUrl() != null ? product.getThumbnailImageUrl() : "(없음)",
            product.getStoryImageUrl() != null ? product.getStoryImageUrl() : "(없음)");
    }


    private String formatReviewInfo(ReviewResponse review) {
        String nickname = review.getReviewer() != null ? review.getReviewer().getNickname() : "(익명)";
        String createdAt = review.getCreatedAt() != null ? review.getCreatedAt().toLocalDate().toString() : "(날짜 없음)";
        return String.format(
            "- 별점: %d점\n- 작성자: %s\n- 작성일: %s\n- 내용: %s",
            review.getStar() != null ? review.getStar() : 0,
            nickname,
            createdAt,
            review.getContent() != null ? review.getContent() : "(내용 없음)");
    }


    private String formatSellerProfile(UserProfileResponse profile) {
        int productCount = profile.getProducts() != null ? profile.getProducts().size() : 0;
        String sellerName = displayName(profile.getNickname(), profile.getName());
        String profileUrl = userProfileUrl(profile.getId());
        String header = String.format(
            "판매자 정보\n- 이름/닉네임: %s\n- 프로필: %s\n- 팔로워: %d명\n- 평균 평점: %.1f\n- 판매중 상품: %d개",
            sellerName,
            profileUrl,
            profile.getFollowerCount() != null ? profile.getFollowerCount() : 0,
            profile.getRatingAvg() != null ? profile.getRatingAvg() : 0.0,
            productCount);

        if (productCount == 0) {
            return header;
        }

        String products = profile.getProducts().stream()
            .limit(5)
            .map(this::formatProductInfo)
            .collect(Collectors.joining("\n\n"));
        return header + "\n\n판매중 상품(최대 5개):\n\n" + products;
    }



    // ----- 파싱 메소드들

    private User resolveAuthenticatedUserOrNull(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return null;
        }

        Object isAuthenticated = toolContext.getContext().get("isAuthenticated");
        if (!(isAuthenticated instanceof Boolean) || !((Boolean) isAuthenticated)) {
            return null;
        }

        Object userIdValue = toolContext.getContext().get("userId");
        if (userIdValue == null) {
            return null;
        }

        Long userId;
        if (userIdValue instanceof Number number) {
            userId = number.longValue();
        } else {
            try {
                userId = Long.parseLong(userIdValue.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return inquiryFundingService.findUserByIdOrNull(userId);
    }

    private Long resolveCategoryIdOrNull(Long categoryId, String categoryName) {
        if (categoryId != null) {
            return categoryId;
        }
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }

        String keyword = categoryName.trim();
        List<CategoryResponse> categories = categoryService.findAll();

        List<CategoryResponse> exactMatches = categories.stream()
            .filter(c -> c.getName() != null && c.getName().equals(keyword))
            .toList();
        if (exactMatches.size() == 1) {
            return exactMatches.getFirst().getId();
        }

        List<CategoryResponse> containsMatches = categories.stream()
            .filter(c -> c.getName() != null && c.getName().contains(keyword))
            .toList();
        if (containsMatches.size() == 1) {
            return containsMatches.getFirst().getId();
        }

        return null;
    }

    private String categoriesHintText() {
        List<CategoryResponse> categories = categoryService.findAll();
        if (categories.isEmpty()) {
            return "(등록된 카테고리 없음)";
        }
        return categories.stream()
            .map(CategoryResponse::getName)
            .collect(Collectors.joining(", "));
    }


    private LocalDateTime parseFlexibleDateTime(String input, LocalTime defaultTime) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("date is blank");
        }
        String value = input.trim();

        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }

        try {
            LocalDate date = LocalDate.parse(value);
            return date.atTime(defaultTime);
        } catch (Exception ignored) {
        }

        Matcher matcher = KOREAN_DATE_PATTERN.matcher(value);
        if (matcher.find()) {
            int y = Integer.parseInt(matcher.group("y"));
            int m = Integer.parseInt(matcher.group("m"));
            int d = Integer.parseInt(matcher.group("d"));
            return LocalDate.of(y, m, d).atTime(defaultTime);
        }

        if (value.equals("오늘")) {
            return LocalDate.now().atTime(defaultTime);
        }
        if (value.equals("내일")) {
            return LocalDate.now().plusDays(1).atTime(defaultTime);
        }
        if (value.equals("모레")) {
            return LocalDate.now().plusDays(2).atTime(defaultTime);
        }

        throw new IllegalArgumentException("unsupported date: " + value);
    }


    private String displayName(String nickname, String name) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        return UNKNOWN;
    }

    // 링크 생성용 메소드
    private String productUrl(Long productId) {
        if (productId == null) {
            return UNKNOWN;
        }
        return FRONT_BASE_URL + "/product/" + productId;
    }

    private String userProfileUrl(Long userId) {
        if (userId == null) {
            return UNKNOWN;
        }
        return FRONT_BASE_URL + "/users/profile/" + userId;
    }
}
