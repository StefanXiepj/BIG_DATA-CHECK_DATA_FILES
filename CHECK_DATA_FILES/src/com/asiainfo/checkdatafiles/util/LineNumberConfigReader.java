package com.asiainfo.checkdatafiles.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class LineNumberConfigReader extends LineNumberReader {

	private int lineNumber = 0;

	public LineNumberConfigReader(Reader in) {
	   super(in);
	}

	@Override
	public int getLineNumber() {
	   return this.lineNumber ;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
	   super.mark(readAheadLimit);
	}

	@Override
	public int read() throws IOException {
	   return super.read();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
	   return super.read(cbuf, off, len);
	}

	@Override
	public String readLine() throws IOException {

	   String l = null ;
	   synchronized (lock){
	    for (;;) {
	     if (this.lineNumber == super.getLineNumber()) {
	      this.lineNumber = super.getLineNumber();
	      l = super.readLine();
	      break ;

	     } else {
	      super.readLine();
	     }
	    }
	    return l ;
	   }
	}

	@Override
	public void reset() throws IOException {
	   super.reset();
	}

	@Override
	public void setLineNumber(int lineNumber) {
	   this.lineNumber = lineNumber ;
	}

	@Override
	public long skip(long n) throws IOException {
	   return super.skip(n);
	}
}
