package com.recruitment.ai.resume.service;

import com.recruitment.ai.client.OpenAiResponsesClient;
import com.recruitment.api.dto.AiResumeParseRequest;
import com.recruitment.api.dto.AiResumeParseResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResumeAiServiceTest {

    @Test
    void shouldReuseResultForIdenticalResumeContent() {
        OpenAiResponsesClient client = mock(OpenAiResponsesClient.class);
        AiResumeParseResponse response = new AiResumeParseResponse();
        response.setOverallScore(80);
        when(client.generateStructured(
                anyString(), anyString(), anyString(), any(), eq(AiResumeParseResponse.class)))
                .thenReturn(response);
        ResumeAiService service = new ResumeAiService(client);

        AiResumeParseRequest firstRequest = new AiResumeParseRequest();
        firstRequest.setResumeContent(" Java developer ");
        AiResumeParseRequest secondRequest = new AiResumeParseRequest();
        secondRequest.setResumeContent("Java developer");

        AiResumeParseResponse first = service.parse(firstRequest);
        AiResumeParseResponse second = service.parse(secondRequest);

        assertSame(first, second);
        verify(client, times(1)).generateStructured(
                anyString(), eq("Java developer"), anyString(), any(), eq(AiResumeParseResponse.class));
    }
}
