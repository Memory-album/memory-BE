package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.dto.response.AnswerResponse;
import com.min.i.memory_BE.domain.album.service.AnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.i.memory_BE.domain.album.dto.request.AnswerRequest;
import com.min.i.memory_BE.domain.album.entity.Answer;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@WebMvcTest(AnswerController.class)
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnswerService answerService;

    private AnswerResponse testAnswerResponse;
    private List<AnswerResponse> testAnswerResponses;

    @BeforeEach
    void setUp() {
        testAnswerResponse = AnswerResponse.builder()
                .id(1L)
                .questionId(1L)
                .userId(1L)
                .content("테스트 답변입니다.")
                .isPrivate(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .userName("테스트 사용자")
                .questionContent("테스트 질문입니다.")
                .build();

        testAnswerResponses = Arrays.asList(testAnswerResponse);
    }

    @Test
    @WithMockUser
    void getAnswersByQuestion_성공() throws Exception {
        // given
        given(answerService.getAnswersByQuestionId(1L))
                .willReturn(testAnswerResponses);

        // when & then
        mockMvc.perform(get("/api/answers/question/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("테스트 답변입니다."));
    }

    @Test
    @WithMockUser
    void getAnswersByUser_성공() throws Exception {
        // given
        given(answerService.getAnswersByUserId(1L))
                .willReturn(testAnswerResponses);

        // when & then
        mockMvc.perform(get("/api/answers/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    @WithMockUser
    void getAnswer_성공() throws Exception {
        // given
        given(answerService.getAnswerById(1L))
                .willReturn(testAnswerResponse);

        // when & then
        mockMvc.perform(get("/api/answers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("테스트 답변입니다."));
    }

    @Test
    @WithMockUser
    void createAnswers_성공() throws Exception {
        // given
        AnswerRequest request = new AnswerRequest();
        request.setQuestionId(1L);
        request.setTextContent("테스트 답변입니다.");

        MockMultipartFile file = new MockMultipartFile(
            "answers",
            "answer.json",
            "application/json",
            new ObjectMapper().writeValueAsString(request).getBytes()
        );

        Answer savedAnswer = Answer.builder()
                .id(1L)
                .content("테스트 답변입니다.")
                .isPrivate(false)
                .build();

        given(answerService.saveAnswer(any(), any(), any(), any()))
                .willReturn(savedAnswer);

        // when & then
        mockMvc.perform(multipart("/api/answers/batch")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.answerCount").value(1));
    }
} 