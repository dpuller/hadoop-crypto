/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.crypto2.hadoop;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;

public final class PathConvertingFileSystemTest {

    private static final Path PATH = new Path("/dummy/path");
    private static final Path DELEGATE_PATH = new Path("/delagate/path");
    private static final Path RETURN_PATH = new Path("/return/path");
    private static final URI DELEGATE_URI = URI.create("/delegate/uri");
    private static final URI RETURN_URI = URI.create("/return/uri");

    private FileSystem delegate;
    private FSDataInputStream inputStream;
    private FSDataOutputStream outputStream;
    private PathConvertingFileSystem convertingFs;

    @Before
    public void before() {
        delegate = mock(FileSystem.class);
        inputStream = mock(FSDataInputStream.class);
        outputStream = mock(FSDataOutputStream.class);
        convertingFs = new PathConvertingFileSystem(delegate, p -> DELEGATE_PATH, p -> RETURN_PATH, u -> RETURN_URI);
    }

    @Test
    public void getUri() throws Exception {
        when(delegate.getUri()).thenReturn(DELEGATE_URI);
        URI actualUri = convertingFs.getUri();

        assertThat(actualUri, is(RETURN_URI));
    }

    @Test
    public void open() throws Exception {
        when(delegate.open(DELEGATE_PATH, 0)).thenReturn(inputStream);
        FSDataInputStream actualStream = convertingFs.open(PATH, 0);

        assertThat(actualStream, is(inputStream));
    }

    @Test
    public void create() throws Exception {
        when(delegate.create(DELEGATE_PATH, null, false, 0, (short) 0, 0, null)).thenReturn(outputStream);
        FSDataOutputStream actualStream = convertingFs.create(PATH, null, false, 0, (short) 0, 0, null);

        assertThat(actualStream, is(outputStream));
    }

    @Test
    public void append() throws Exception {
        when(delegate.append(DELEGATE_PATH, 0, null)).thenReturn(outputStream);
        FSDataOutputStream actualStream = convertingFs.append(PATH, 0, null);

        assertThat(actualStream, is(outputStream));
    }

    @Test
    public void rename() throws Exception {
        when(delegate.rename(DELEGATE_PATH, DELEGATE_PATH)).thenReturn(false);
        boolean success = convertingFs.rename(PATH, PATH);

        assertThat(success, is(false));
    }

    @Test
    public void delete() throws Exception {
        when(delegate.delete(DELEGATE_PATH, false)).thenReturn(false);
        boolean success = convertingFs.delete(PATH, false);

        assertThat(success, is(false));
    }

    @Test
    public void makeQualified() {
        when(delegate.makeQualified(DELEGATE_PATH)).thenReturn(DELEGATE_PATH);
        Path path = convertingFs.makeQualified(PATH);

        assertThat(path, is(RETURN_PATH));
    }

    @Test
    public void listStatus() throws Exception {
        when(delegate.listStatus(DELEGATE_PATH)).thenReturn(new FileStatus[] {fileStatus(DELEGATE_PATH)});
        FileStatus[] fileStatuses = convertingFs.listStatus(PATH);

        assertThat(Arrays.asList(fileStatuses), contains(fileStatus(RETURN_PATH)));
    }

    @Test
    public void getFileStatus() throws Exception {
        when(delegate.getFileStatus(DELEGATE_PATH)).thenReturn(fileStatus(DELEGATE_PATH));
        FileStatus fileStatus = convertingFs.getFileStatus(PATH);

        assertThat(fileStatus, is(fileStatus(RETURN_PATH)));
    }

    @Test
    public void setWorkingDirectory() throws Exception {
        convertingFs.setWorkingDirectory(PATH);
        verify(delegate).setWorkingDirectory(DELEGATE_PATH);
    }

    @Test
    public void getWorkingDirectory() throws Exception {
        when(delegate.getWorkingDirectory()).thenReturn(DELEGATE_PATH);
        Path workingDirectory = convertingFs.getWorkingDirectory();

        assertThat(workingDirectory, is(RETURN_PATH));
    }

    @Test
    public void mkdirs() throws Exception {
        when(delegate.mkdirs(DELEGATE_PATH, null)).thenReturn(false);
        boolean success = convertingFs.mkdirs(PATH, null);

        assertThat(success, is(false));
    }

    private static FileStatus fileStatus(Path path) {
        return new FileStatus(0, false, 0, 0, 0, path);
    }

}
