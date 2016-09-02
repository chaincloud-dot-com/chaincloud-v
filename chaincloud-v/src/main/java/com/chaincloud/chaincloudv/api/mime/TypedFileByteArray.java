package com.chaincloud.chaincloudv.api.mime;

import retrofit.mime.TypedByteArray;

/**
 * Created by songchenwen on 15/8/11.
 */
public class TypedFileByteArray extends TypedByteArray {
    private String fileName;

    public TypedFileByteArray(String fileName, String mimeType, byte[] bytes) {
        super(mimeType, bytes);
        this.fileName = fileName;
    }

    @Override
    public String fileName() {
        return fileName;
    }
}
