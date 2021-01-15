package eu.opertusmundi.web.utils;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import lombok.Getter;

public class ReturnValueCaptor<T> implements Answer<T> {

    @Getter
    private T result = null;

    @SuppressWarnings("unchecked")
    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        this.result = (T) invocationOnMock.callRealMethod();
        return this.result;
    }
}