package org.exoplatform.services.jcr.analyzer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class IgnoreSentencesEndFilter extends TokenFilter {
  
  protected IgnoreSentencesEndFilter(TokenStream input) {
    super(input);
  }
  public final Token next() throws java.io.IOException {
    Token nextToken = input.next();
    if (nextToken != null) {
      String tokenText = nextToken.termText();
      tokenText = tokenText.replaceAll("([\\.,;:]+$)", "");
//      System.out.println("tokenText1: " + tokenText1 + "      tokenText: " + tokenText);
      if(tokenText.equals("")||tokenText.trim().equals("")){
        return new Token("", 0, 0, nextToken.type());
      }
      return new Token(tokenText.trim(),nextToken.startOffset(), nextToken.startOffset()+tokenText.length(), nextToken.type());
    } else
      return null;
  }
}