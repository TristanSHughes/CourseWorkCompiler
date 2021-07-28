import java.io.*;
import java.util.*;
public class Compiler {

     static HashMap<String,String> typeTable=new HashMap<String,String>();
     static HashMap<String,Node> ftable=new HashMap<String,Node>();
     static HashMap<String,Boolean> valueTable=new HashMap<String,Boolean>();
     static HashMap<String,String> inputTable=new HashMap<String,String>();
     static HashMap<String,Integer> depthTable=new HashMap<String,Integer>();
    final static String keywords[]={"and","add","if","while","input","num","proc","or","sub","then","for","output","bool","T","not","mult","else","eq","halt","string","F"};
    final static char symbolKey[]={'<','>','(',')','{','}','=',',',';'};
   static boolean quote=false;
  static  int quoteCount=0;
  static  int tokenCount=0;
   static int row=1;
    static int column=0;
   static boolean keyword=false;
   static boolean userDef=false;
   static boolean intLit=false;
   static boolean stringLit=false;
   static boolean symbol = false;
   static boolean negate=false;
    static boolean zero=false;
   static String lastToken="";
   static FileWriter writer;
   static class Node{
    String value;
    ArrayList<Node> children=new ArrayList();
    Node parent;
    int number;
    scopeNode scope;
    String type="none";
    boolean checked=false;
    boolean hasval=false;
    public void checkAll()
    {
        checked=true;
        for (int i=0; i<children.size(); i++)
        {
            children.get(i).checkAll();
        }
    }
    public void unCheck()
    {
        checked=false;
        for(int i=0; i<children.size();i++)
            children.get(i).unCheck();
    }
    public void setType(String newType)
    {
        type=newType;
    }
    public void check()
    {
        checked=true;
    }
    public String getValue()
    {
        return value;
    }
    public void setValue(String newValue)
    {
        value=newValue;
    }
    public void setNumber(int num)
    {
        number=num;
    }
    public int getNumber()
    {
        return number;
    }
    public void setParent(Node newParent)
    {
        parent=newParent;
    }
    public Node getParent()
    {
        return parent;
    }
    public void addChild(Node newChild)
    {
        children.add(newChild);
    }
    public void outputSymbol(FileWriter symbolWriter) throws Exception
    {
      symbolWriter.write(number+": "+value+"\n");  
    }
    public void output(FileWriter outputWriter,FileWriter symbolWriter, FileWriter treeWriter) throws Exception
    {
        //code in full output for all files
        tree.write(number+": ");
        for(int i=0; i<children.size()-1;i++)
        { 
            tree.write(children.get(i).getNumber()+",");
        }
        if(children.size()>0)
        tree.write(children.get(children.size()-1).getNumber()+"\n");
        else
        tree.write("\n");
        
        for(int x=0; x<children.size();x++)
        {
            children.get(x).output(outputWriter, symbolWriter, treeWriter);
        }
        
    }
    
    public void outputScope(FileWriter symbolWriter) throws Exception
    {
      symbolWriter.write(number+": "+value+" "+scope.scope+"\n");
      for(int i=0; i<children.size(); i++)
      {
          children.get(i).outputScope(symbolWriter);
      }
    }
    public void outputType(FileWriter symbolWriter) throws Exception
    {
       symbolWriter.write(number+": "+value+" "+type+" "+checked+"\n");
      for(int i=0; i<children.size(); i++)
      {
          children.get(i).outputType(symbolWriter);
      } 
    }
    public void outputVal(FileWriter symbolWriter) throws Exception
    {
        
        symbolWriter.write(number+": "+value+" "+type+" "+hasval+"\n");
      for(int i=0; i<children.size(); i++)
      {
          children.get(i).outputVal(symbolWriter);
      } 
        
    }
    public void printStructure(String indentation,boolean isLast)
    {
        System.out.print(indentation);
        if(isLast)
        {
            System.out.print(">");
            indentation+=" ";
        }else
        {
            System.out.print("|>");
            indentation+="| ";
        }
        System.out.println(number+": "+value+" "+type);
        for(int i=0; i<children.size();i++)
        {
            children.get(i).printStructure(indentation, i==children.size()-1);
        }
    }
    public void setScope(scopeNode currscope)
    {
        scope=currscope;
    }
   
}
  static String word;
  static String token;
  static Scanner inputScan;
  static Node root;
  static Node currParent;
  static FileWriter output;
  static FileWriter symbols;
  static FileWriter tree;
  static int counter; 
  static class scopeNode{
      ArrayList<String> variableNames=new ArrayList<String>();
      ArrayList<String> inheritedNames=new ArrayList<String>();
      ArrayList<String> varReplacers=new ArrayList<String>();
      ArrayList<String> inheritedReplacers=new ArrayList<String>();
      ArrayList<scopeNode> children=new ArrayList<scopeNode>();
      scopeNode upperNode=null;
      String scope="";
      String value="";
      int scopeLevel;
      public void setScope(String newScope)
      {
          scope=newScope;
      }
      public void addVarName(String name)
      {
          variableNames.add(name);
      }
      public void addVarReplacer(String replacer)
      {
          varReplacers.add(replacer);
      }
      public void addChildren(scopeNode newChild)
      {
          children.add(newChild);
      }
      public void setUpperNode(scopeNode newUpper)//call only once on node creation....never again and never anywhere else
      {
          upperNode=newUpper;
          for(int i=0; i<upperNode.variableNames.size(); i++)
          {
              inheritedNames.add(upperNode.variableNames.get(i));
              inheritedReplacers.add(upperNode.varReplacers.get(i));
          }
          if(upperNode.inheritedNames.size()!=0&&upperNode.inheritedReplacers.size()!=0)
          for(int j=0; j<upperNode.inheritedNames.size();j++)
          {
              inheritedNames.add(upperNode.inheritedNames.get(j));
              inheritedReplacers.add(upperNode.inheritedReplacers.get(j));
              
          }
          
      }
      
      
  }
  //intermediate language generation
  public static FileWriter relative;
  public static FileWriter finalCode;
  public static HashMap<String, Integer> procedureLines=new HashMap<String,Integer>();
  public static int linenum=0;
  public static int labelnum=0;
  public static void generateBasic()
  {
      
      root.unCheck();
      try{
      relative=new FileWriter(new File("relative.bas"));
      finalCode=new FileWriter(new File("source.bas"));
      //call other methods
      translate(root);
      relative .write("ENDPROGRAM:");
      finalCode.write("99999");
      relative.close();
      finalCode.close();
      }catch(Exception e)
      {
          e.printStackTrace();
      }
      
  }
  public static void translate(Node currentNode) throws Exception
  {
      
      if(currentNode.checked)
    {
        for(int i=0; i<currentNode.children.size(); i++)
        {
            translate(currentNode.children.get(i));
        }
        return;
    }
    if(currentNode.value.equals("CALL"))
    {
        translateCall(currentNode);
    }
    if(currentNode.value.equals("VAR"))
    {
      translateVar(currentNode);  
    }
    if(currentNode.value.equals("VAR_EQ"))
    {
        translateVarEq(currentNode);
       
    }
    if(currentNode.value.equals("CALCADD")||currentNode.value.equals("CALCMULT")||currentNode.value.equals("CALCSUB"))
    {
        translateCalc(currentNode);
    }
    if(currentNode.value.equals("BOOL"))
    {
        translateBool(currentNode);
    }
    if(currentNode.value.equals("COND_BRANCH"))
    {
        translateCond(currentNode);
    }
    if(currentNode.value.equals("NUMEXPR"))
    {
        translateNumexpr(currentNode);
    }
    if(currentNode.value.equals("COND_LOOP"))
    {
        translateLoop(currentNode);
    }
    if(currentNode.value.equals("IO input")||currentNode.value.equals("IO output"))
    {
        translateIO(currentNode);
    }
    if(currentNode.value.equals("PROC"))
    {
        translateProc(currentNode);
    }
    if(currentNode.value.equals("halt"))
    {
        translateHalt(currentNode);
    }
    currentNode.check();
    for(int i=0; i<currentNode.children.size(); i++)
        {
            translate(currentNode.children.get(i));
        }
      
  }
  public static void translateCall(Node currentNode)throws Exception//generates whole line
  {
      linenum++;
      int currline=linenum;
      currentNode.check();
      String name=currentNode.children.get(0).value.substring(9);
      relative.write("GOSUB "+name+"\n");
      finalCode.write(currline+" GOSUB "+procedureLines.get(name));
  }
  public static String translateVar(Node currentNode)//does not generate line
  {
      currentNode.check();
      String name=currentNode.children.get(0).value.substring(9);
      if(currentNode.type.equals("S"))
          name=name+"$";
      return name;
  }
  public static String translateCalc(Node currentNode) throws Exception//generate line
  {
      currentNode.check();
      if(currentNode.value.equals("CALCADD"))
      {
          return translateNumexpr(currentNode.children.get(0))+"+"+translateNumexpr(currentNode.children.get(1));
      }
      if (currentNode.value.equals("CALCSUB"))
      {
           return translateNumexpr(currentNode.children.get(0))+"-"+translateNumexpr(currentNode.children.get(1));
      }
      if(currentNode.value.equals("CALCMULT"))
      {
           return translateNumexpr(currentNode.children.get(0))+"*"+translateNumexpr(currentNode.children.get(1));
      }
      return "";
  }
  public static String translateNumexpr(Node currentNode)throws Exception//does not generate line, but can generate intermediate lines
  {
      currentNode.check();
      if(currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
      {
          //linenum++;
          varCount++;
          String assignVal=translateCalc(currentNode.children.get(0));
          linenum++;
          relative.write("v"+varCount+"="+assignVal+"\n");
          finalCode.write(linenum+" "+"v"+varCount+"="+assignVal+"\n");
          return "v"+varCount;
      }
      if(currentNode.children.get(0).value.equals("VAR"))
      {
          return translateVar(currentNode.children.get(0));
      }
      if(currentNode.children.get(0).value.length()>7&&currentNode.children.get(0).value.substring(0,7).equals("integer"))
      {
          return currentNode.children.get(0).value.substring(8);
      }
     return ""; 
  }
  public static void translateCond(Node currentNode)throws Exception//generates line
  {
      currentNode.check();
      String assignVal=translateBool(currentNode.children.get(0));
      linenum++;
      labelnum++;
      int truelabel=labelnum;
      labelnum++;
      int falselabel=labelnum;
      labelnum++;
      int endlabel=labelnum;
      int trueLine=linenum+5;
      int falseLine=trueLine+200;
      int endLine=falseLine+200;
      if(currentNode.children.size()==3)
       endLine=falseLine+200;
      relative.write("IF "+assignVal+" THEN GOTO TRUE"+truelabel+"\n");
      finalCode.write(linenum+" IF "+assignVal+" THEN GOTO "+trueLine+"\n");
      linenum++;
      if(currentNode.children.size()==3)
      {
      relative.write("GOTO FALSE"+falselabel+"\n");
      finalCode.write(linenum+" GOTO "+falseLine+"\n");
      }else
      {
        relative.write("GOTO ENDIF"+endlabel+"\n");
      finalCode.write(linenum+" GOTO "+endLine+"\n");  
      }
      linenum=trueLine;
      relative.write("TRUE"+truelabel+":\n");
          finalCode.write(linenum+"\n");
      translate(currentNode.children.get(1));
      linenum++;
      relative.write("GOTO ENDIF"+endlabel+"\n");
      finalCode.write(linenum+" GOTO "+endLine+"\n");
      if(currentNode.children.size()==3)
      {
          linenum=falseLine;
          relative.write("FALSE"+falselabel+":\n");
          finalCode.write(linenum+"\n");
          translate(currentNode.children.get(2));
          linenum=endLine;
          //labelnum++;
          //endlabel=labelnum;
          relative.write("ENDIF"+endlabel+":\n");
          finalCode.write(linenum+"\n");
      }
      else
      {
          linenum=endLine;
          relative.write("ENDIF"+endlabel+":\n");
          finalCode.write(linenum+"\n");
      }
      
      
  }
  public static void translateLoop(Node currentNode)throws Exception//generates line
  {
      currentNode.check();
      String assignVal;
      int startLine;
      int endLine;
      int trueLine;
      int startlabel;
      int endlabel;
      int truelabel;
      if(currentNode.children.get(0).value.equals("BOOL"))//while loop
      {
          
          assignVal=translateBool(currentNode.children.get(0));
          linenum++;
          labelnum++;
          startlabel=labelnum;
          labelnum++;
          truelabel=labelnum;
          labelnum++;
          endlabel=labelnum;
          startLine=linenum;
          trueLine=linenum+2;
          endLine=startLine+200;
          relative.write("START"+startlabel+": IF "+assignVal+" THEN GOTO TRUE"+truelabel+"\n");
          finalCode.write(linenum+" IF "+assignVal+" THEN GOTO "+trueLine+"\n");
          linenum++;
          relative.write("GOTO ENDWHILE"+endlabel+"\n");
          finalCode.write(linenum+" GOTO "+endLine+"\n");
          linenum=trueLine;
          relative.write("TRUE"+truelabel+":\n");
          finalCode.write(linenum+"\n");
          translate(currentNode.children.get(1));
          linenum=endLine-1;
          relative.write("GOTO START"+startlabel+"\n");
          finalCode.write("GOTO "+startLine+"\n");
          linenum++;
          relative.write("ENDWHILE"+endlabel+":\n");
          finalCode.write(linenum+"\n");
          return;
          
          
      }
      //for loop in book
      String checkVar=translateVar(currentNode.children.get(0));
      String gateVar=translateVar(currentNode.children.get(2));
       labelnum++;
          startlabel=labelnum;
          labelnum++;
          truelabel=labelnum;
          labelnum++;
          endlabel=labelnum;
      linenum++;
      relative.write(checkVar+"=0\n");
      finalCode.write(linenum+" "+checkVar+"=0\n");
      linenum++;
      varCount++;
      assignVal="v"+varCount;
      relative.write(assignVal+"="+checkVar+"<"+gateVar+"\n");
      finalCode.write(linenum+" "+assignVal+"="+checkVar+"<"+gateVar+"\n");
      linenum++;
      startLine=linenum;
      trueLine=startLine+2;
      endLine=startLine+200;
      relative.write("START"+startlabel+": IF "+assignVal+" THEN GOTO TRUE"+truelabel+"\n");
      finalCode.write(linenum+" IF "+assignVal+" THEN GOTO "+trueLine+"\n");
      linenum++;
      relative.write("GOTO ENDFOR"+endlabel+"\n");
      finalCode.write(linenum+" GOTO "+endLine+"\n");
      linenum=trueLine;
      relative.write("TRUE"+truelabel+":\n");
      finalCode.write(linenum+"\n");
      translate(currentNode.children.get(5));
      linenum++;
      relative.write(checkVar+"="+checkVar+"+1\n");
      finalCode.write(linenum+" "+checkVar+"="+checkVar+"+1\n");
      linenum++;
      relative.write(assignVal+"="+checkVar+"<"+gateVar+"\n");
      finalCode.write(linenum+" "+assignVal+"="+checkVar+"<"+gateVar+"\n");
      linenum=endLine-1;
      relative.write("GOTO START"+startlabel+"\n");
      finalCode.write("GOTO "+startLine+"\n");
      linenum++;
      relative.write("ENDFOR"+endlabel+":\n");
      finalCode.write(linenum+"\n");
      
  }
  public static String translateBool(Node currentNode) throws Exception//does not generate line
  {
      currentNode.check();
      if(currentNode.children.get(0).value.equals("BOOLSETEQ")||currentNode.children.get(0).value.equals("BOOLSETOR")||currentNode.children.get(0).value.equals("BOOLSETAND"))
      {
          return translateBoolSet(currentNode.children.get(0));
      }
      if(currentNode.children.get(0).value.equals("VAR")&&currentNode.children.size()==1)
      {
          return translateVar(currentNode.children.get(0));
      }
      if(currentNode.children.get(0).value.equals("BOOLNOT"))//gets treated the same as bool but append not to it
      {
         return "NOT "+translateBool(currentNode.children.get(0)); 
      }
      if(currentNode.children.get(0).value.equals("VAR"))
      {
          linenum++;
          varCount++;
          if(currentNode.children.get(1).value.equals("LTHAN"))
          {
             relative.write("v"+varCount+"="+translateVar(currentNode.children.get(0))+"<"+translateVar(currentNode.children.get(1).children.get(0))+"\n");
             finalCode.write(linenum+" "+"v"+varCount+"="+translateVar(currentNode.children.get(0))+"<"+translateVar(currentNode.children.get(1).children.get(0))+"\n");
             return "v"+varCount;
          }
          if(currentNode.children.get(1).value.equals("GTHAN"))
          {
              relative.write("v"+varCount+"="+translateVar(currentNode.children.get(0))+">"+translateVar(currentNode.children.get(1).children.get(0))+"\n");
             finalCode.write(linenum+" "+"v"+varCount+"="+translateVar(currentNode.children.get(0))+">"+translateVar(currentNode.children.get(1).children.get(0))+"\n");
             return "v"+varCount;
              
          }
      }
      if(currentNode.children.get(0).value.equals("boolean T"))
      {
         return "1"; 
      }
      if(currentNode.children.get(0).value.equals("boolean F"))
      {
          return "0";
      }
      
      return "";
  }
  public static String translateBoolSet(Node currentNode) throws Exception//does  generate line
  {
      
      currentNode.check();
      if(currentNode.value.equals("BOOLSETEQ"))
      {
          linenum++;
          varCount++;
          int currVar=varCount;
          relative.write("v"+currVar+"="+translateVar(currentNode.children.get(0))+"="+translateVar(currentNode.children.get(1))+"\n");
          finalCode.write(linenum+" "+"v"+currVar+"="+translateVar(currentNode.children.get(0))+"="+translateVar(currentNode.children.get(1))+"\n");
          return "v"+currVar;
          
      }
      if(currentNode.value.equals("BOOLSETOR"))
      {
          
          String assignVal;
          String assignVal2=translateBool(currentNode.children.get(1));
          assignVal=translateBool(currentNode.children.get(0));
          varCount++;
          int currVar=varCount;
          linenum++;
          int TruelineNum=linenum+10;
          int endLineNum=TruelineNum+5;
          labelnum++;
          int truelabel=labelnum;
          labelnum++;
          int endlabel=labelnum;
          relative.write("IF "+assignVal+" THEN GOTO TRUE"+truelabel+"\n");
          finalCode.write(linenum+" IF"+assignVal+" THEN GOTO "+TruelineNum+"\n");
          linenum++;
           relative.write("IF "+assignVal2+" THEN GOTO TRUE"+truelabel+"\n");
          finalCode.write(linenum+" IF"+assignVal2+" THEN GOTO "+TruelineNum+"\n");
          linenum++;
          relative.write("v"+currVar+"=0"+"\n");
          finalCode.write(linenum+" v"+currVar+"=0"+"\n");
          linenum++;
          relative.write("GOTO ENDIF"+endlabel+" \n");
          finalCode.write(linenum+" GOTO "+endLineNum+"\n");
          linenum=TruelineNum;
          relative.write("TRUE"+truelabel+": "+"v"+currVar+"=1\n");
          finalCode.write(linenum+" v"+currVar+"=1\n");
          linenum=endLineNum;
          relative.write("ENDIF"+endlabel+": \n");
          finalCode.write(linenum+"\n");
          return "v"+currVar;
          
          
          
      }
      if(currentNode.value.equals("BOOLSETAND"))
      {
          String assignVal;
          String assignVal2=translateBool(currentNode.children.get(1));
          assignVal=translateBool(currentNode.children.get(0));
          varCount++;
          int currVar=varCount;
          linenum++;
          int falseLineNum=linenum+10;
          int trueLineNum=falseLineNum+5;
          int endLineNum=trueLineNum+5;
          int falselabel;
          int truelabel;
          int endlabel;
          labelnum++;
          truelabel=labelnum;
          labelnum++;
          falselabel=labelnum;
          labelnum++;
          endlabel=labelnum;
          int nextlabel;
          labelnum++;
          nextlabel=labelnum;
          relative.write("IF "+assignVal+" THEN GOTO NEXT"+nextlabel+"\n");
          finalCode.write(linenum+" IF"+assignVal+" THEN GOTO "+(linenum+2)+"\n");
          linenum++;
          relative.write("GOTO FALSE"+falselabel+"\n");
          finalCode.write(linenum+"GOTO "+falseLineNum);
          linenum++;
          relative.write("IF "+assignVal2+" THEN GOTO TRUE"+truelabel+"\n");
          finalCode.write(linenum+" IF"+assignVal2+" THEN GOTO "+trueLineNum+"\n");
          linenum++;
          relative.write("GOTO FALSE"+falselabel+"\n");
          finalCode.write(linenum+"GOTO "+falseLineNum);
          linenum=falseLineNum;
          relative.write("FALSE"+falselabel+": v"+currVar+"=0\n");
          finalCode.write(linenum+" v"+currVar+"=0\n");
          linenum++;
          relative.write("GOTO ENDIF"+endlabel+" \n");
          finalCode.write(linenum+" GOTO "+endLineNum+"\n");
          linenum=trueLineNum;
          relative.write("TRUE"+truelabel+": "+"v"+currVar+"=1\n");
          finalCode.write(linenum+" v"+currVar+"=1\n");
          linenum=endLineNum;
          relative.write("ENDIF"+endlabel+": \n");
          finalCode.write(linenum+"\n");
      }
      return "";
  }
  public static void translateProc(Node currentNode) throws Exception//generates line
  {
      currentNode.check();
      linenum++;
      labelnum++;
      int skiplabel=labelnum;
      relative.write("GOTO SKIPDECL"+skiplabel+"\n");
      int skipnum=labelnum+200;
      finalCode.write(linenum+" GOTO "+skipnum+"\n");
      linenum++;
      relative.write(currentNode.children.get(0).value+":\n");
      procedureLines.put(currentNode.children.get(0).value,linenum);
      finalCode.write(linenum);
      
      translate(currentNode.children.get(1));
      linenum++;
      relative.write("RETURN\n");
      finalCode.write(linenum+" "+"RETURN\n");
      linenum=skipnum;
      relative.write("SKIPDECL"+skiplabel+":\n");
      finalCode.write(linenum+"\n");
      
      //create label in intermediate and line number in final
      
  }
  public static void translateDecl(Node currentNode) throws Exception//generates line
  {
      //nuffink I think
  }
  public static void translateHalt(Node currentNode) throws Exception//generates one line
  {
      currentNode.check();
      linenum++;
      relative.write("GOTO ENDPROGRAM\n");
      finalCode.write(linenum+" GOTO 99999");
  }
  public static void translateIO(Node currentNode) throws Exception//generates line
  {
      currentNode.check();
      linenum++;
      if(currentNode.value.equals("IO input"))
      {
          relative.write("INPUT"+" "+translateVar(currentNode.children.get(0))+"\n" );
          finalCode.write(linenum+" INPUT"+" "+translateVar(currentNode.children.get(0))+"\n");
          if(currentNode.children.get(0).type.equals("B"))
          {
              linenum++;
              relative.write(translateVar(currentNode.children.get(0))+"="+translateVar(currentNode.children.get(0))+"=1\n");
              finalCode.write(linenum+" "+translateVar(currentNode.children.get(0))+"="+translateVar(currentNode.children.get(0))+"=1\n");
          }
      }
      if(currentNode.value.equals("IO output"))
      {
         relative.write("PRINT"+" "+translateVar(currentNode.children.get(0))+"\n" );
          finalCode.write(linenum+" PRINT"+" "+translateVar(currentNode.children.get(0))+"\n"); 
      }
      
  }
  public static void translateVarEq(Node currentNode) throws Exception//generates line
  {
      currentNode.check();
      //linenum++;
      //int currLine=linenum;
      String assignVal;
      if(currentNode.children.get(0).value.equals("VAR"))
      {
         linenum++;
      relative.write(translateVar(currentNode.parent.children.get(0))+"="+translateVar(currentNode.children.get(0))+"\n");
      finalCode.write(linenum+" "+translateVar(currentNode.parent.children.get(0))+"="+translateVar(currentNode.children.get(0))+"\n");
      }
      if(currentNode.children.get(0).value.equals("NUMEXPR"))
      {
          assignVal=translateNumexpr(currentNode.children.get(0));
          linenum++;
         relative.write(translateVar(currentNode.parent.children.get(0))+"="+assignVal+"\n");
      finalCode.write(linenum+" "+translateVar(currentNode.parent.children.get(0))+"="+assignVal+"\n"); 
      }
      if(currentNode.children.get(0).value.equals("BOOL"))
      {
          assignVal=translateBool(currentNode.children.get(0));
          linenum++;
          relative.write(translateVar(currentNode.parent.children.get(0))+"="+assignVal+"\n");
      finalCode.write(linenum+" "+translateVar(currentNode.parent.children.get(0))+"="+assignVal+"\n");
          
      }
      if(currentNode.children.get(0).value.length()>8&&currentNode.children.get(0).value.substring(0,9).equals("stringLit"))
      {
          linenum++;
          relative.write(translateVar(currentNode.parent.children.get(0))+"="+currentNode.children.get(0).value.substring(10)+"\n");
      finalCode.write(linenum+" "+translateVar(currentNode.parent.children.get(0))+"="+currentNode.children.get(0).value.substring(10)+"\n");
          
      }
      
  }
  
  
  public static boolean withinLoop=false;
  public static boolean inElse=false;
  public static int loopDepth=0;
public static void valCheck()
{
  root.unCheck();
  valTraverse(root);
  System.out.println(valueTable.toString());
  System.out.println(inputTable.toString());
  System.out.println(depthTable.toString());
  System.out.println("Value check complete");
      try{
      symbols=new FileWriter("Valsymbols.txt");
  }catch(Exception e)
  {
      System.out.println(e.getMessage()+" issue initialising file");
  }
    
   try{
      root.outputVal(symbols);
      symbols.close();
      }catch(Exception er)
      {
          System.out.println(er.getMessage()+"issue outputing to file");
      }
  
}
public static void valTraverse(Node currentNode)
{
    if(currentNode.checked)
    {
        for(int i=0; i<currentNode.children.size(); i++)
        {
            valTraverse(currentNode.children.get(i));
        }
        return;
    }
    if(currentNode.value.equals("CALL"))
    {
        valCheckCall(currentNode);
    }
    if(currentNode.value.equals("VAR"))
    {
      valCheckVar(currentNode);  
    }
    if(currentNode.value.equals("VAR_EQ"))
    {
        valCheckAssign(currentNode);
       
    }
    if(currentNode.value.equals("BOOL"))
    {
        valCheckBool(currentNode);
    }
    if(currentNode.value.equals("COND_BRANCH"))
    {
        valCheckCond(currentNode);
    }
    if(currentNode.value.equals("NUMEXPR"))
    {
        valCheckNumexpr(currentNode);
    }
    if(currentNode.value.equals("COND_LOOP"))
    {
        valCheckLoop(currentNode);
    }
    if(currentNode.value.equals("IO input")||currentNode.value.equals("IO output"))
    {
        valCheckIO(currentNode);
    }
    if(currentNode.value.equals("PROC"))
    {
        currentNode.checkAll();
    }
    currentNode.check();
    for(int i=0; i<currentNode.children.size(); i++)
        {
            valTraverse(currentNode.children.get(i));
        }
    
}

public static void valCheckCall(Node currentNode)
{
    //System.out.println("performing call check");
    currentNode.check();
    String name=currentNode.children.get(0).value.substring(9);
    Node posNode;
    
   
    posNode=ftable.get(name);
    //System.out.println(posNode.value);
     posNode.unCheck();
    valTraverse(posNode.children.get(1));
}
public static void valCheckIO(Node currentNode)
{
    //System.out.println("performing io check ");
    currentNode.check();
    
    if(currentNode.value.equals("IO input"))
    {
       
       valAssignVar(currentNode.children.get(0));
    }
    if(currentNode.value.equals("IO output"))
    {
        currentNode.hasval=true;
        if(!valCheckVar(currentNode.children.get(0)))
        {
            currentNode.hasval=false;
            System.out.println("IO variable "+currentNode.children.get(0).children.get(0).value+" has no value");
        }
    }
    
}

public static void valCheckAssign(Node currentNode)
{
    //System.out.println("performing assign check");
    currentNode.check();
    int currentDepth=loopDepth;
    currentNode.parent.children.get(0).children.get(0).check();
    String name;
    //can be var numexpr bool or stringlit
    if(currentNode.children.get(0).value.equals("VAR"))
    {
        name=currentNode.parent.children.get(0).children.get(0).value.substring(9);
        if(valCheckVar(currentNode.children.get(0)))
        {
            currentNode.hasval=true;
            //if(!withinLoop)
            valueTable.put(name,true);
            String value;
            
            if(currentNode.children.get(0).value.length()>8&&currentNode.children.get(0).value.substring(0,9).equals("stringlit"))
            {
                if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    value=inputTable.get(name)+";"+currentNode.children.get(0).value.substring(10);
                else
                    value=currentNode.children.get(0).value.substring(10);
               inputTable.put(name,value);
               depthTable.put(name,currentDepth);
            }
            if(currentNode.children.get(0).value.equals("NUMEXPR"))
            {
                if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    value=inputTable.get(name)+";"+retrieveNumExprValue(currentNode.children.get(0));
                else
                    value=retrieveNumExprValue(currentNode.children.get(0));
               inputTable.put(name,value); 
               depthTable.put(name,currentDepth);
            }
            if(currentNode.children.get(0).value.equals("VAR"))
            {
                if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    value=inputTable.get(name)+";"+retrieveVarValue(currentNode.children.get(0));
                else
                    value=retrieveVarValue(currentNode.children.get(0));
               inputTable.put(name,value);
               depthTable.put(name,currentDepth);
            }
            if(currentNode.children.get(0).value.equals("BOOL"))
            {
                if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    value=inputTable.get(name)+";"+retrieveBoolValue(currentNode.children.get(0));
                else
                    value=retrieveBoolValue(currentNode.children.get(0));
                inputTable.put(name,value);
                depthTable.put(name,currentDepth);
            }
            
            return;
        }
        else
        {
            valueTable.put(name,false);
            currentNode.hasval=false;
            System.out.println("Variables cannot assigned to non-valued variables "+name);
            return;
        }
    }
    if(currentNode.children.get(0).value.equals("NUMEXPR"))
    {
        name=currentNode.parent.children.get(0).children.get(0).value.substring(9);
        if(valCheckNumexpr(currentNode.children.get(0)))
        {
            currentNode.hasval=true;
         // if(!withinLoop)
         valueTable.put(name,true);
         String val;
         if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    val=inputTable.get(name)+";"+retrieveNumExprValue(currentNode.children.get(0));
                else
                    val=retrieveNumExprValue(currentNode.children.get(0));
        
         inputTable.put(name, val);
         depthTable.put(name,currentDepth);
         return;
        }else
        {
            valueTable.put(name,false);
            currentNode.hasval=false;
            System.out.println("Variables cannot assigned to non-valued num expressions "+name);
            return;
        }
        
    }
    if(currentNode.children.get(0).value.equals("BOOL"))
    {
       name=currentNode.parent.children.get(0).children.get(0).value.substring(9);
       if(valCheckBool(currentNode.children.get(0)))
       {
           currentNode.hasval=true;
           //if(!withinLoop)
           valueTable.put(name,true);
           String val;
         if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    val=inputTable.get(name)+";"+retrieveBoolValue(currentNode.children.get(0));
                else
                    val=retrieveBoolValue(currentNode.children.get(0));
        
         inputTable.put(name, val);
         depthTable.put(name,currentDepth);
           return;
       }else
       {
           valueTable.put(name,false);
           currentNode.hasval=false;
           System.out.println("Variables cannot assigned to non-valued booleans "+name);
           return;
       }
    }
    if(currentNode.children.get(0).value.length()>8&&currentNode.children.get(0).value.substring(0,9).equals("stringLit"))
    {
        currentNode.hasval=true;
       name=currentNode.parent.children.get(0).children.get(0).value.substring(9);
      // if(!withinLoop)
       valueTable.put(name, true);
       String val;
         if((withinLoop||inElse)&&!inputTable.get(name).equals("null")&&currentDepth>=depthTable.get(name))
                    val=inputTable.get(name)+";"+currentNode.children.get(0).value.substring(10);
                else
                    val=currentNode.children.get(0).value.substring(10);
        
         inputTable.put(name, val);
         depthTable.put(name,currentDepth);
       
    }
    
}
public static boolean valCheckNumexpr(Node currentNode)
{
    
    //System.out.println("performing numexpr check");
    //System.out.println(currentNode.children.get(0).value);
    currentNode.check();
    if(currentNode.children.get(0).value.equals("VAR"))
    {
        if(valCheckVar(currentNode.children.get(0)))
        {
            currentNode.hasval=true;
            return true;
        }else
            return false;
    }
    if(currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
    {
        if(valCheckCalc(currentNode.children.get(0)))
        {
            currentNode.hasval=true;
            return true;
        }else
            return false;
    }
    if(currentNode.children.get(0).value.length()>7&&currentNode.children.get(0).value.substring(0,7).equals("integer"))
    {
        //System.out.println("Yeet");
        currentNode.hasval=true;
        return true;
    }
    return false;
}
public static boolean valCheckCalc(Node currentNode)
{
   // System.out.println("performing calc check");
    currentNode.check();
    boolean hasval1=false;
    boolean hasval2=false;
    hasval1=valCheckNumexpr(currentNode.children.get(0));
    hasval2=valCheckNumexpr(currentNode.children.get(1));
    //System.out.println("owo");
    //System.out.println(hasval1);
    //System.out.println(hasval2);
    if(hasval1&&hasval2)
    {
        currentNode.hasval=true;
        return true;
    }
    return false;
}
public static void valCheckCond(Node currentNode)//think about how to do this too
{
    //System.out.println("performing cond check");
    int currentDepth=loopDepth;
    currentNode.check();
    if(valCheckBool(currentNode.children.get(0)))
    {
        currentNode.hasval=true;
       String status=retrieveBoolValue(currentNode.children.get(0));
       if(status.equals("T"))
       {
          // withinLoop=true;
           //for(int i=1; i<currentNode.children.size(); i++)
           //{
           loopDepth++;
               valTraverse(currentNode.children.get(1));
               if(currentNode.children.size()==3)
               {
                   currentNode.children.get(2).checkAll();
               }
               loopDepth--;
               //withinLoop=true;
          // }
           //withinLoop=false;
          
       }else
       {
           if(status.equals("F"))
           {
           currentNode.children.get(1).checkAll();
            if(currentNode.children.size()==3)
           {
               loopDepth++;
               
               //withinLoop=true;
               valTraverse(currentNode.children.get(2));
               //withinLoop=false;
               loopDepth--;
           }
           }else
           {
               loopDepth++;
              withinLoop=true;
              
                  valTraverse(currentNode.children.get(1));
                  
              if(currentDepth==0)
              withinLoop=false;
              
              if(currentNode.children.size()==3)
              {
                  inElse=true;
                  valTraverse(currentNode.children.get(2));
                  if(currentDepth==0)
                  inElse=false;
                  
              }
              loopDepth--;
              
           }
           
       }
        
    }else
    {
       System.out.println("Boolean condition requires value "+currentNode.children.get(0).value); 
    }
}
public static boolean valCheckBool(Node currentNode)
{
    //System.out.println("performing bool check");
    currentNode.check();
    if(currentNode.children.get(0).value.equals("BOOLSET")||currentNode.children.get(0).value.equals("BOOLSETEQ")||currentNode.children.get(0).value.equals("BOOLSETOR")||currentNode.children.get(0).value.equals("BOOLSETAND"))
    {
        currentNode.hasval=valCheckBoolSet(currentNode.children.get(0));
        return valCheckBoolSet(currentNode.children.get(0));
    }
    if(currentNode.children.get(0).value.equals("BOOL"))
    {
        currentNode.hasval=valCheckBool(currentNode.children.get(0));
       return valCheckBool(currentNode.children.get(0)); 
    }
    if(currentNode.children.get(0).value.equals("BOOLNOT"))
    {
        currentNode.hasval=valCheckBool(currentNode.children.get(0));
        return valCheckBool(currentNode.children.get(0));
    }
    if(currentNode.children.get(0).value.equals("VAR")&&currentNode.children.size()>1)
    {
        boolean val1=false;
        boolean val2=false;
        val1=valCheckVar(currentNode.children.get(0));
        currentNode.children.get(1).check();
        currentNode.children.get(1).children.get(0).check();
        val2=valCheckVar( currentNode.children.get(1).children.get(0));
        if(val1&&val2)
        {
            currentNode.hasval=true;
            return true;
        }
    }
    if(currentNode.children.get(0).value.equals("boolean T")||currentNode.children.get(0).value.equals("boolean F"))
    {
        currentNode.hasval=true;
        return true;
    }
    if(currentNode.children.get(0).value.equals("VAR"))
    {
        currentNode.hasval=valCheckVar(currentNode.children.get(0));
        return valCheckVar(currentNode.children.get(0));
    }
    
    return false;
}
public static boolean valCheckBoolSet(Node currentNode)
{
    //System.out.println("performing bool set check");
    currentNode.check();
    boolean val1=false;
    boolean val2=false;
    if(currentNode.children.get(0).value.equals("VAR"))
    {
      val1=valCheckVar(currentNode.children.get(0));  
    }
    if(currentNode.children.get(0).value.equals("BOOL")||currentNode.children.get(0).value.equals("BOOLNOT"))
    {
        val1=valCheckBool(currentNode.children.get(0));
    }
    if(currentNode.children.get(1).value.equals("VAR"))
    {
        val2=valCheckVar(currentNode.children.get(1));
    }
    if(currentNode.children.get(1).value.equals("BOOL")||currentNode.children.get(1).value.equals("BOOLNOT"))
    {
        val2=valCheckBool(currentNode.children.get(1));
    }
    if(val1&&val2)
    {
        currentNode.hasval=true;
        return true;
    }
    return false;
    
    
}
public static void valCheckLoop(Node currentNode)
{
    //System.out.println("performing loop check");
    //just hard code this
    int currentDepth=loopDepth;
    currentNode.check();
    if(currentNode.children.get(0).value.equals("BOOL"))
    {
        currentNode.hasval=true;
       if(valCheckBool(currentNode.children.get(0)))
       {
           String value=retrieveBoolValue(currentNode.children.get(0));
           //System.out.println("melm");
           if(value.equals("T"))
           {
             //withinLoop=true;
             for(int i=1; i<currentNode.children.size();i++)
             {
                 loopDepth++;
                 valTraverse(currentNode.children.get(i));
                 loopDepth--;
                 //withinLoop=true;
             }
            // withinLoop=false;
           }else
           {
               if(value.equals("F"))
               currentNode.children.get(1).checkAll();
               else
               {
                    withinLoop=true;
                    loopDepth++;
                    for(int i=1; i<currentNode.children.size();i++)
                    {
                        valTraverse(currentNode.children.get(i));
                        withinLoop=true;
                    }
                    loopDepth--;
                        if(currentDepth==0)
                        withinLoop=false;
                   
               }
           }
           
       }
       else
       {
           System.out.println("Boolean must have value for conditional loop: "+currentNode.children.get(0).value);
       }
    }
    if(currentNode.children.get(0).value.equals("VAR")&&currentNode.children.size()>2)
    {
        //figure out value checking
        String name1;
        boolean fail=false;
        /*String name2;
        String name3;
        String name4;
        String name5;*/
        name1=currentNode.children.get(0).children.get(0).value.substring(9);
        /*name2=currentNode.children.get(1).children.get(0).value.substring(9);
        name3=currentNode.children.get(2).children.get(0).value.substring(9);
        name4=currentNode.children.get(3).children.get(0).value.substring(9);
        name5=currentNode.children.get(4).children.get(0).value.substring(9);*/
        valueTable.put(name1,true);
        inputTable.put(name1,"0");
        if(valCheckVar(currentNode.children.get(0))&&valCheckVar(currentNode.children.get(1))&&valCheckVar(currentNode.children.get(2))&&valCheckVar(currentNode.children.get(3))&&valCheckCalc(currentNode.children.get(4)))
        {
            currentNode.hasval=true;
            String val1;
            String val2;
            String string1=retrieveVarValue(currentNode.children.get(1));
            String string2=retrieveVarValue(currentNode.children.get(2));
            //System.out.println("moop");
            //System.out.println(string1);
            //System.out.println(string2);
            StringTokenizer val1token=new StringTokenizer(string1,";");
           // System.out.println("meep");
            while(val1token.hasMoreTokens()&&!fail)
            {
                val1=val1token.nextToken();
               // System.out.println(val1);
                StringTokenizer val2token=new StringTokenizer(string2,";");
                while(val2token.hasMoreTokens()&&!fail)
                {
                    val2=val2token.nextToken();
                  //  System.out.println(val2);
           //add code for string tokenizer and while loop
           int num1;
           int num2;
                    if(val1.equals("null"))
                    {
                        fail=true;
                        break;
                    }
                    else
                    num1=Integer.parseInt(val1);
                    if(val2.equals("null"))
                    {
                        fail=true;
                        break;
                    }
                    else
                    num2=Integer.parseInt(val2);
                    if(num1>=num2)
                    {
                        currentNode.checkAll();
                        fail=true;
                    }
                }
            }
            if(fail)
            {
               currentNode.checkAll();
            }
            else
            {
                valTraverse(currentNode.children.get(5));
            }
        }
        else {
            System.out.println("All booleans must have value for conditional for loop: "+currentNode.children.get(0).children.get(0).value+" "+currentNode.children.get(1).children.get(0).value+" "+currentNode.children.get(2).children.get(0).value+" "+currentNode.children.get(3).children.get(0).value+" "+currentNode.children.get(4).children.get(0).value);
        }
    }
}
public static boolean valCheckVar(Node currentNode)
{
    //System.out.println("performing var check");
    currentNode.check();
    String name=currentNode.children.get(0).value.substring(9);
   // System.out.println(name);
   currentNode.hasval=valueTable.get(name);
    return valueTable.get(name);
        
}
public static void valAssignVar(Node currentNode)
{
   currentNode.check();
   String name=currentNode.children.get(0).value.substring(9);
   
   valueTable.put(name,true);
   //add code for inputTable
}
public static String retrieveBoolValue(Node currentNode)
{
    //alter base code to differentiate between or and not lthan and gthan
    String inter1;
    String inter2;
    if(currentNode.children.size()==2 && (currentNode.children.get(1).value.equals("GTHAN")||currentNode.children.get(1).value.equals("LTHAN")))
    {
        String string1=retrieveVarValue(currentNode.children.get(0));
        String string2=retrieveVarValue(currentNode.children.get(1).children.get(0));
       int val1;
       int val2;
       StringTokenizer token1=new StringTokenizer(string1,";");
       while(token1.hasMoreTokens()){
           inter1=token1.nextToken();
           if(inter1.equals("null"))
               return "null";
           val1=Integer.parseInt(inter1);
       StringTokenizer token2=new StringTokenizer(string2,";");
       while(token2.hasMoreTokens())
       {
           inter2=token2.nextToken();
           if(inter2.equals("null"))
               return"null";
       val2=Integer.parseInt(inter2);
       if(currentNode.children.get(1).value.equals("GTHAN"))
       {
           if(val1<=val2)
           {
               return "F";
           }
           
         
       }
       if(currentNode.children.get(1).value.equals("LTHAN"))
       {
         if(val1>=val2)
         {
             return "F";
         }
           
       }
       }
       }
       return "T";
    }
   /* if(currentNode.children.get(0).equals("VAR"))
    {
        return retrieveVarValue(currentNode.children.get(0));
    }*/
    if(currentNode.children.get(0).equals("BOOLSETEQ"))
    {
        return retrieveBoolsetValue(currentNode.children.get(0));
    }/*
    if(currentNode.children.get(0).equals("BOOLNOT"))
    {
        String bool=retrieveBoolValue(currentNode.children.get(0));
        if(bool.equals("T"))
            return "F";
        else
            return "T";
    }
    if(currentNode.children.get(0).value.substring(0,6).equals("boolean"))
    {
        return currentNode.children.get(0).value.substring(8);
    }*/
    return "";
}
public static String retrieveBoolsetValue(Node currentNode)
{
    String val1;
    String val2;
    /*if(currentNode.value.equals("BOOLSETAND"))
    {
        val1=retrieveBoolValue(currentNode.children.get(0));
        val2=retrieveBoolValue(currentNode.children.get(1));
        if(val1.equals("T")&&val2.equals("T"))
        {
            return "T";
        }
        return "F";
    }
    if(currentNode.value.equals("BOOLSETOR"))
    {
       val1=retrieveBoolValue(currentNode.children.get(0));
        val2=retrieveBoolValue(currentNode.children.get(1));
        if(val1.equals("T")||val2.equals("T"))
        {
            return "T";
        }
        return "F"; 
    }*/
    if(currentNode.value.equals("BOOLSETEQ"))
    {
      val1=retrieveVarValue(currentNode.children.get(0));
        val2=retrieveVarValue(currentNode.children.get(1));
        if(val1.equals(val2))
        {
            return "T";
        }
        return "F";   
    }
    
    return "";
    
}
public static String retrieveVarValue(Node currentNode)
{
    String name=currentNode.children.get(0).value.substring(9);
    //System.out.println(name);
    return inputTable.get(name);
}
public static String retrieveNumExprValue(Node currentNode)
{
    if(currentNode.children.get(0).value.equals("VAR"))
    {
        return retrieveVarValue(currentNode.children.get(0));
    }
    if(currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
    {
        //return retrieveCalcValue(currentNode.children.get(0));
        return "null";
    }
    if(currentNode.children.get(0).value.length()>7&&currentNode.children.get(0).value.substring(0,7).equals("integer"))
    {
        return currentNode.children.get(0).value.substring(8);
    }
    return "";
}
public static String retrieveCalcValue(Node currentNode)
{
    //add code to differentiate between math ops
    int val1=Integer.parseInt(retrieveNumExprValue(currentNode.children.get(0)));
    int val2=Integer.parseInt(retrieveNumExprValue(currentNode.children.get(1)));
    String answer;
    if(currentNode.value.equals("CALCADD"))
    {
        answer=Integer.toString(val1+val2);
        return answer;
    }
    if(currentNode.value.equals("CALCMULT"))
    {
        answer=Integer.toString(val1*val2);
        return answer;
    }
    if(currentNode.value.equals("CALCSUB"))
    {
        answer=Integer.toString(val1-val2);
        return answer;
    }
    return "";
}
public static void TypeCheck()
{
    DeclCheck(root);
    //System.out.println("lmao decl works");
    navigateTree(root);
    //System.out.println("lmao navigat works");
    try{
      symbols=new FileWriter("Typesymbols.txt");
  }catch(Exception e)
  {
      System.out.println(e.getMessage()+" issue initialising file");
  }
    
   try{
      root.outputType(symbols);
      symbols.close();
      }catch(Exception er)
      {
          System.out.println(er.getMessage()+"issue outputing to file");
      }
   root.printStructure("", true);
    
    
    
}
public static void navigateTree(Node currentNode)
{
    if(currentNode.checked)
    {
        for(int i=0; i<currentNode.children.size(); i++)
            navigateTree(currentNode.children.get(i));
        return;
    }
    if(currentNode.value.equals("VAR"))
    {
        varCheck(currentNode);
        
    }
    if(currentNode.value.equals("VAR_EQ"))
    {
        var_eqCheck(currentNode);
    }
    if(currentNode.value.equals("NUMEXPR"))
    {
        numExprCheck(currentNode);
    }
    if(currentNode.value.equals("CALC")||currentNode.value.equals("CALCADD")||currentNode.value.equals("CALCMULT")||currentNode.value.equals("CALCSUB"))
    {
        calcCheck(currentNode);
    }
    if(currentNode.value.equals("COND_BRANCH"))
    {
        condCheck(currentNode);
    }
    if(currentNode.value.equals("BOOL")||currentNode.value.equals("BOOLNOT"))
    {
        boolCheck(currentNode);
    }
    if(currentNode.value.equals("BOOLSET")||currentNode.value.equals("BOOLSETEQ")||currentNode.value.equals("BOOLSETAND")||currentNode.value.equals("BOOLSETOR"))
    {
        boolsetCheck(currentNode);
    }
    if(currentNode.value.equals("COND_LOOP"))
    {
        condLoopCheck(currentNode);
    }
    if(currentNode.value.equals("IO input")||currentNode.value.equals("IO output"))
    {
        IOCheck(currentNode);
    }
    if(currentNode.value.equals("CALL"))
    {
        callCheck(currentNode);
    }
    
    
    
    //final fall back case
    if(!currentNode.checked)
    {
        currentNode.check();
        
    }
    for(int i=0; i<currentNode.children.size(); i++)
            navigateTree(currentNode.children.get(i));
}
public static void DeclCheck(Node currentNode)
{
    if (currentNode.value.equals("DECL"))
    {
        if(currentNode.children.get(0).children.get(0).value.substring(5).equals("string"))
        {
            currentNode.children.get(0).setType("S");
            currentNode.check();
            currentNode.children.get(0).check();
        }
        if(currentNode.children.get(0).children.get(0).value.substring(5).equals("num"))
        {
            currentNode.children.get(0).setType("N");
            currentNode.check();
            currentNode.children.get(0).check();
        }
        if(currentNode.children.get(0).children.get(0).value.substring(5).equals("bool"))
        {
            currentNode.children.get(0).setType("B");
            currentNode.check();
            currentNode.children.get(0).check();
        }
        currentNode.children.get(1).setType(currentNode.children.get(0).type);
        currentNode.children.get(1).check();
        currentNode.children.get(1).children.get(0).setType(currentNode.children.get(1).type);
        currentNode.children.get(1).children.get(0).check();
        typeTable.put(currentNode.children.get(1).children.get(0).value.substring(5),currentNode.children.get(1).type);
        valueTable.put(currentNode.children.get(1).children.get(0).value.substring(5),false);
        inputTable.put(currentNode.children.get(1).children.get(0).value.substring(5),"null");
        depthTable.put(currentNode.children.get(1).children.get(0).value.substring(5),0);
       // System.out.println(currentNode.children.get(1).children.get(0).value.substring(5)+" "+currentNode.children.get(1).type);
    }
    if (currentNode.value.equals("PROC"))
    {
        currentNode.children.get(0).setType("P");
        currentNode.children.get(0).check();
        currentNode.check();
        typeTable.put(currentNode.children.get(0).value,currentNode.children.get(0).type);
        ftable.put(currentNode.children.get(0).value,currentNode);
        
    }
    /*if(currentNode.value.substring(0,1).equals("IO"))
    {
        String curval=currentNode.children.get(0).children.get(0).value;
        if(typeTable.containsKey(curval)&&(typeTable.get(curval).equals("N")||typeTable.get(curval).equals("B")||typeTable.get(curval).equals("S")))
        {
            currentNode.children.get(0).children.get(0).type=typeTable.get(curval);
        }
        else
        {
            System.out.println("IO error Invalid type at symbol: "+currentNode.value);
        }
    }*/
    //add in later function 
    /*if(currentNode.value.equals("CALL"))
    {
        String curval=currentNode.children.get(0).value.substring(8);
        if(typeTable.containsKey(curval)&&typeTable.get(curval).equals("P"))
        {
            currentNode.children.get(0).type=typeTable.get(curval);
        }else
        {
         System.out.println("Call error not of type procedure: "+currentNode.children.get(0).value);   
        }
    }*/
   /* if(currentNode.value.equals("VAR_EQ"))
    {
        //first check var that is parent
        String currVal=currentNode.parent.children.get(0).children.get(0).value;
        String currType;
        if(typeTable.containsKey(currVal))
        {
          currType  
        }else
        {
            System.out.println("Var type error: "+currVal);
        }
    }*/
    
    
    
    
    
    
    for(int i=0; i<currentNode.children.size();i++)
    {
        DeclCheck(currentNode.children.get(i));
    }
    
}
public static void varCheck(Node currentNode)
{
   if(currentNode.value.equals("VAR"))
   {
        currentNode.check();
        currentNode.children.get(0).check();
       String currVal=currentNode.children.get(0).value.substring(9);
      // System.out.println(currVal);
       if(typeTable.containsKey(currVal))
       {
           //System.out.println("owo");
          currentNode.children.get(0).type=typeTable.get(currVal);
          currentNode.type=typeTable.get(currVal);
       }
   }
  /* for(int i=0; i<currentNode.children.size();i++)
   {
        varCheck(currentNode.children.get(i));
   }*/
   
}
public static void var_eqCheck(Node currentNode)
{
    String type1;
    String type2;
    if(currentNode.value.equals("VAR_EQ"))
    {
        currentNode.check();
        if(currentNode.parent.children.get(0).checked==false)
        varCheck(currentNode.parent.children.get(0));
      type1=currentNode.parent.children.get(0).type;
      if(currentNode.children.get(0).value.length()>=9&&currentNode.children.get(0).value.substring(0,9).equals("stringLit"))
          type2="S";
      else{
          if(currentNode.children.get(0).value.equals("VAR"))
       varCheck(currentNode.children.get(0));
          if(currentNode.children.get(0).value.equals("NUMEXPR"))
              numExprCheck(currentNode.children.get(0));
          if(currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
          {
              calcCheck(currentNode.children.get(0));
          }
          if(currentNode.children.get(0).value.equals("BOOL")||currentNode.children.get(0).value.equals("BOOLNOT"))
              boolCheck(currentNode.children.get(0));
      type2=currentNode.children.get(0).type;
      }
      if(!type1.equals(type2))
      {
          System.out.println("Type mismatch: "+type1+" cannot be matched to type "+type2 +currentNode.parent.children.get(0).number+ " "+currentNode.children.get(0).number );
      }
    }
   /* for(int i=0; i<currentNode.children.size();i++)
   {
        var_eqCheck(currentNode.children.get(i));
   }*/
}
public static void numExprCheck(Node currentNode)
{
    if(currentNode.value.equals("NUMEXPR"))
    {
        currentNode.check();
        if(currentNode.children.get(0).value.equals("VAR")||currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
        {
            if(currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
            {
                calcCheck(currentNode.children.get(0));
            }
            if(currentNode.children.get(0).value.equals("VAR"))
            {
                varCheck(currentNode.children.get(0));
            }
            if(currentNode.children.get(0).type.equals("N"))
            {
                currentNode.type="N";
            }else
            {
                System.out.println("Type mismatch: "+currentNode.children.get(0).type);
            }
            
        }else
        if(currentNode.children.get(0).value.substring(0,7).equals("integer"))
        {
            currentNode.children.get(0).check();
            currentNode.type="N";
        }else{
            System.out.println("Invalid type value: "+currentNode.children.get(0).type+" "+currentNode.children.get(0).number);
        }
        
        
    }
   /* for(int i=0; i<currentNode.children.size();i++)
   {
        numExprCheck(currentNode.children.get(i));
   }*/
    
}

public static void calcCheck(Node currentNode)
{
    
    if(currentNode.value.equals("CALC")||currentNode.value.equals("CALCADD")||currentNode.value.equals("CALCMULT")||currentNode.value.equals("CALCSUB"))
    {
        currentNode.check();
        String type1;
        String type2;
        if(currentNode.children.get(0).value.equals("NUMEXPR"))
        {
          numExprCheck(currentNode.children.get(0));  
        }
        if(currentNode.children.get(1).value.equals("NUMEXPR"))
        {
           numExprCheck(currentNode.children.get(1)); 
        }
        if(currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
        {
            calcCheck(currentNode.children.get(0));
        }
        if(currentNode.children.get(1).value.equals("CALC")||currentNode.children.get(1).value.equals("CALCADD")||currentNode.children.get(1).value.equals("CALCMULT")||currentNode.children.get(1).value.equals("CALCSUB"))
        {
            calcCheck(currentNode.children.get(1));
        }
        
        type1=currentNode.children.get(0).type;
        type2=currentNode.children.get(1).type;
        if(type1.equals("N")&&type2.equals("N"))
        {
           currentNode.type="N"; 
        }else
        {
            System.out.println("Error type mismatch: "+currentNode.children.get(0).type+" "+currentNode.children.get(1).type+" "+currentNode.children.get(0).number+" "+currentNode.children.get(1).number);
        }
        
    }
    /*for(int i=0; i<currentNode.children.size();i++)
   {
        calcCheck(currentNode.children.get(i));
   }*/
}
public static void condCheck(Node currentNode)
{
    if(currentNode.value.equals("COND_BRANCH"))
    {
        currentNode.check();
        boolCheck(currentNode.children.get(0));
       if(currentNode.children.get(0).type.equals("B"))
       {
           
       }
       else
       {
           System.out.println("Expected type B in cond branch"+currentNode.children.get(0).number+" "+currentNode.children.get(0).type);
       }
    }
   /* for(int i=0; i<currentNode.children.size();i++)
    {
        condCheck(currentNode.children.get(i));
    }*/
    
}
public static void boolCheck(Node currentNode)
{
    if(currentNode.value.equals("BOOL")||currentNode.value.equals("BOOLNOT"))
    {
        currentNode.check();
        if(currentNode.children.get(0).value.equals("BOOLSET")||currentNode.children.get(0).value.equals("BOOLSETEQ")||currentNode.children.get(0).value.equals("BOOLSETAND")||currentNode.children.get(0).value.equals("BOOLSETOR"))
        {
          boolsetCheck(currentNode.children.get(0));  
          if(currentNode.children.get(0).type.equals("B"))
              currentNode.type="B";
        }
        if(currentNode.children.size()==2&&(currentNode.children.get(1).value.equals("GTHAN")||currentNode.children.get(1).value.equals("LTHAN")))//double check this 
        {
            String type1;
            String type2;
            varCheck(currentNode.children.get(0));
            varCheck(currentNode.children.get(1).children.get(0));
            type1=currentNode.children.get(0).type;
            type2=currentNode.children.get(1).children.get(0).type;
            if(type1.equals(type2)&&!type1.equals("none"))
            {
              currentNode.type="B";  
            }
            else{
                System.out.println("Type mismatch "+currentNode.children.get(0).type+" "+currentNode.children.get(1).children.get(0).type+" "+currentNode.children.get(0).number+" "+currentNode.children.get(1).children.get(0).number);
            }
        }
        if(currentNode.children.get(0).value.equals("BOOL")||currentNode.children.get(0).value.equals("BOOLNOT"))
        {
          boolCheck(currentNode.children.get(0));
          if(currentNode.children.get(0).type.equals("B"))
          {
              currentNode.type="B";
          }else
          {
              System.out.println("Expected type B "+currentNode.children.get(0).type+" "+currentNode.children.get(0).number);
          }
        }
        if(currentNode.children.get(0).value.equals("boolean T")||currentNode.children.get(0).value.equals("boolean F"))
        {
            currentNode.type="B";
        }
        if(currentNode.children.get(0).value.equals("VAR")&&currentNode.children.size()==1)
        {
            varCheck(currentNode.children.get(0));
            if(currentNode.children.get(0).type.equals("B"))
            {
                currentNode.type="B";
            }
            else
            {
                System.out.println("Expected type of B "+currentNode.children.get(0).type+" "+currentNode.children.get(0).number);
            }
        }
    }
   /* for(int i=0; i<currentNode.children.size(); i++)
    {
        boolCheck(currentNode.children.get(i));
    }*/
}
public static void boolsetCheck(Node currentNode)
{
    if(currentNode.value.equals("BOOLSET")||currentNode.value.equals("BOOLSETOR")||currentNode.value.equals("BOOLSETAND"))
    {
        currentNode.check();
        if(currentNode.children.get(0).value.equals("VAR"))
            varCheck(currentNode.children.get(0));
        else
        boolCheck(currentNode.children.get(0));
        if(currentNode.children.get(1).value.equals("VAR"))
            varCheck(currentNode.children.get(1));
        else
        boolCheck(currentNode.children.get(1));
        if(currentNode.children.get(0).type.equals("B")&&currentNode.children.get(1).type.equals("B"))
        {
           currentNode.type="B"; 
        }else
        {
            System.out.println("Type mismatch "+currentNode.children.get(0).type+" "+currentNode.children.get(1).type);
        }
    }
    if(currentNode.value.equals("BOOLSETEQ"))
    {//add functionality to check types here
        currentNode.check();
        if(currentNode.children.get(0).value.equals("VAR"))
        varCheck(currentNode.children.get(0));
        if(currentNode.children.get(0).value.equals("CALC")||currentNode.children.get(0).value.equals("CALCADD")||currentNode.children.get(0).value.equals("CALCMULT")||currentNode.children.get(0).value.equals("CALCSUB"))
         calcCheck(currentNode.children.get(0));
        if(currentNode.children.get(0).value.equals("NUMEXPR"))
            numExprCheck(currentNode.children.get(0));
        if(currentNode.children.get(1).value.equals("VAR"))
        varCheck(currentNode.children.get(1));
        if(currentNode.children.get(1).value.equals("CALC")||currentNode.children.get(1).value.equals("CALCADD")||currentNode.children.get(1).value.equals("CALCMULT")||currentNode.children.get(1).value.equals("CALCSUB"))
         calcCheck(currentNode.children.get(1));
        if(currentNode.children.get(1).value.equals("NUMEXPR"))
            numExprCheck(currentNode.children.get(1));
        String type1=currentNode.children.get(0).type;
        String type2=currentNode.children.get(1).type;
        if(type1.equals(type2))
        {
            currentNode.type="B";
        }else{
            System.out.println("Type mismatch "+currentNode.children.get(0).type+" "+currentNode.children.get(1).type);
        }
    }
    
}

public static void condLoopCheck(Node currentNode)
{
    if(currentNode.value.equals("COND_LOOP"))
    {
        currentNode.check();
        if(currentNode.children.size()==2)
        {
           boolCheck(currentNode.children.get(0));
           if(currentNode.children.get(0).type.equals("B"))
               currentNode.type="B";
           else
               System.out.println("Expected type bool "+currentNode.children.get(0).type+" "+currentNode.children.get(0).number);
        }else
        {
           //add functionality for for loop
            varCheck(currentNode.children.get(0));
            varCheck(currentNode.children.get(1));
            varCheck(currentNode.children.get(2));
            varCheck(currentNode.children.get(3));
            calcCheck(currentNode.children.get(4));
            String type1=currentNode.children.get(0).type;
            String type2=currentNode.children.get(1).type;
            String type3=currentNode.children.get(2).type;
            String type4=currentNode.children.get(3).type;
            String type5=currentNode.children.get(4).type;
            if(type1.equals("N")&&type2.equals("N")&&type3.equals("N")&&type4.equals("N")&&type5.equals("N"))
            {
                
            }
            else{
                System.out.println("Expected int type but int not provided "+type1+" "+type2+" "+type3+" "+type4+" "+type5);
            }
        }
        
    }
}
public static void IOCheck(Node currentNode)
{
   if(currentNode.value.equals("IO input")||currentNode.value.equals("IO ouptput"))
   {
       currentNode.check();
       varCheck(currentNode.children.get(0));
       if(currentNode.children.get(0).type.equals("N")||currentNode.children.get(0).type.equals("B")||currentNode.children.get(0).type.equals("S"))
       {
           
       }else{
           System.out.println("Incorrect type in IO statement.");
       }
   }
}
public static void callCheck(Node currentNode)
{
   if(currentNode.value.equals("CALL"))
   {
       currentNode.check();
     String curval=currentNode.children.get(0).value.substring(9);
        if(typeTable.containsKey(curval)&&typeTable.get(curval).equals("P"))
        {
            currentNode.children.get(0).type=typeTable.get(curval);
        }else
        {
         System.out.println("Call error not of type procedure: "+currentNode.children.get(0).value+" "+currentNode.children.get(0).number);   
        }  
   }
}
//add code for IO
//add output sysstem

  static int scopeCount=0;
      static int varCount=0;
     // static scopeNode currentScope;
  public static void Scope()
  {     try{
      symbols=new FileWriter("Newsymbols.txt");
  }catch(Exception e)
  {
      System.out.println(e.getMessage()+" issue initialising file");
  }
      scopeNode scopeRoot=new scopeNode();
      scopeRoot.setScope("P"+scopeCount);
      scopeRoot.value="global";
      scopeRoot.scopeLevel=0;
      scopeCount++;
      //currentScope=scopeRoot;
      //implement any other set up code
        
      traverseTree(root,scopeRoot);
      funcDetect(root,scopeRoot);
   
      try{
      root.outputScope(symbols);
      symbols.close();
      }catch(Exception er)
      {
          System.out.println(er.getMessage()+"issue outputing to file");
      }
      
  }
  
  public static void traverseTree(Node currentNode,scopeNode currentScope)
  {
       //System.out.println("OWO");
      scopeNode currScope=currentScope;
      if(Objects.equals(currentNode.getValue(),"PROC"))//recode to understand new format
      {
          Node funcNode=currentNode.children.get(0);
          StringTokenizer funcParser=new StringTokenizer(funcNode.getValue());
          String funcName=funcParser.nextToken();
          funcName=funcParser.nextToken();
         // System.out.println(funcName);
         for(int i=0; i<currentScope.children.size();i++)
         {
             if(currentScope.children.get(i).value.equals(funcName)||currentScope.value.equals(funcName))
             {
                System.out.println("Redefinintion error: "+ currentNode.getValue());
              try{
              end();
              }catch(Exception er)
              {
                  System.out.println(er.getMessage()+"issue shutting down after error");
              } 
             }
         }
         if(currentScope.upperNode!=null)
             for(int j=0; j<currentScope.upperNode.children.size();j++)
             {
                 if(currentScope.upperNode.children.get(j).value.equals(funcName))
                 {
                       System.out.println("Redefinintion error: "+ currentNode.getValue());
                        try{
                         end();
                        }catch(Exception er)
                        {
                            System.out.println(er.getMessage()+"issue shutting down after error");
                            } 
                     
                 }
             }
          scopeNode newScope=new scopeNode();
          newScope.setScope("P"+scopeCount);
          newScope.scopeLevel=currentScope.scopeLevel+1;
          scopeCount++;
          newScope.value=funcName;
          newScope.setUpperNode(currentScope);
          currentScope.addChildren(newScope);
          funcNode.value=newScope.scope;
          for(int i=0; i<currentNode.children.size();i++)
          {
              traverseTree(currentNode.children.get(i),newScope);
          }
          currentNode.setScope(currScope);
          //currentScope=newScope;
          return;
          //create new scope
      }else{
      //StringTokenizer valueParser=new StringTokenizer(currentNode.getValue());
      //String valueToken=valueParser.nextToken();
      if(Objects.equals(currentNode.value,"DECL"))
      {
          boolean exists=false;
          Node declareNode=currentNode.children.get(1).children.get(0);
          //System.out.println(declareNode.getValue());
          StringTokenizer nameParser=new StringTokenizer(declareNode.getValue());
                String nameToken=nameParser.nextToken();
                nameToken=nameParser.nextToken();
               // System.out.println(nameToken);
          for(int i=0; i<currentScope.variableNames.size();i++)
          {
              
              if(Objects.equals(nameToken,currentScope.variableNames.get(i)))
              {
                  exists=true;//check if it exists in current scope
                  break;
              }
          }
          if(!exists)
          {
              //add to list
              currentScope.addVarName(nameToken);
              currentScope.addVarReplacer("v"+varCount);
              declareNode.setValue("name v"+varCount);
              varCount++;
          }else
          {
              ///output redefinition error
              System.out.println("Redefinintion error: "+ currentNode.getValue());
              try{
              end();
              }catch(Exception er)
              {
                  System.out.println(er.getMessage()+"issue shutting down after error");
              }
          }
          //add new variable to scope variable table and replace the old one if it already exists;
          
      }
      else
      {
          if(currentNode.getValue().equals("VAR"))
          {
              int x=-1;
              boolean localdefined=false;
              boolean inheritDefined=false;
               Node varNode=currentNode.children.get(0);
          StringTokenizer nameParser=new StringTokenizer(varNode.getValue());
                String varToken=nameParser.nextToken();
                varToken=nameParser.nextToken();
                for(int i=0; i<currentScope.variableNames.size(); i++)
                {
                    //System.out.println(varToken);
                    if(currentScope.variableNames.get(i).equals(varToken))
                    {
                       localdefined=true;
                       x=i;
                       break;
                    }
                }
                if(localdefined)
                {
                    varNode.setValue("variable "+currentScope.varReplacers.get(x));
                }else
                {
                    for(int j=0;j<currentScope.inheritedNames.size();j++)
                    {
                    if(currentScope.inheritedNames.get(j).equals(varToken))
                    {
                       inheritDefined=true;
                       x=j;
                       break;
                    }
                        
                    }
                    if(inheritDefined)
                    {
                       varNode.setValue("variable "+currentScope.inheritedReplacers.get(x)); 
                    }else{
                     varNode.setValue("variable U");
                     //try{
                       // end();
                        //}catch(Exception er)
                       // {
                         //   System.out.println(er.getMessage()+"issue with variable recognition");
                        //}
                    }
                }
              
              
          }
          
          //assign scope to node as it is not changed for functions or instructions
      }
      }
      for(int i=0; i<currentNode.children.size();i++)
      {
          traverseTree(currentNode.children.get(i),currScope);
      }
      currentNode.setScope(currScope);
      return;
    
  }
  public static String funcFind(String funcName,int scopeLvl,scopeNode currentScope)
  {if(currentScope.scopeLevel<=scopeLvl)
  {
      if(funcName.equals(currentScope.value))
      {
          return currentScope.scope;
      }
      for(int i=0; i<currentScope.children.size();i++)
      {
        String replacer=funcFind(funcName,scopeLvl,currentScope.children.get(i));
        if(!replacer.equals(""))
            return replacer;
      }
      
  }
  
      return"";
  }
  
  public static void funcDetect(Node currentNode,scopeNode scopeRoot)
  {
      scopeNode currentScope;
      currentScope=currentNode.scope;
   if(Objects.equals(currentNode.getValue(),"CALL"))
   {
       boolean isdefined=false;
       String replacer;
       replacer=currentScope.scope;
       Node funcNode=currentNode.children.get(0);
       StringTokenizer funcToken=new StringTokenizer(funcNode.getValue());
       String funcName=funcToken.nextToken();
       funcName=funcToken.nextToken();
       scopeNode testNode=funcNode.scope;
       //if(testNode.upperNode!=null)
       for(int i=0; i<testNode.children.size();i++)
       {
           if(funcName.equals(testNode.children.get(i).value))
           {
               replacer=testNode.children.get(i).scope;
               isdefined=true;
               break;
           }
       }
       //while(testNode!=null&&!isdefined)
       if(!isdefined)
       {
           //System.out.println(funcName);
           //System.out.println(testNode.value);
          /* if(funcName.equals(testNode.value))
           {
               replacer=testNode.scope;
               isdefined=true;
               //break;
           }
           testNode=testNode.upperNode;*/
           replacer=funcFind(funcName,testNode.scopeLevel,scopeRoot);
           if(!replacer.equals(""))
               isdefined=true;
       }
       if(isdefined)
       {
         funcNode.setValue("function "+replacer);
       }
       else{
          funcNode.setValue("function U"); 
       }
   }
   for (int i=0; i<currentNode.children.size(); i++)
   {
       funcDetect(currentNode.children.get(i),scopeRoot);
   }
  }


  
    public static void parse()throws Exception
    {
        File infile=new File("output.txt");
     inputScan=new Scanner (infile);
     tree=new FileWriter("tree.txt");
     symbols=new FileWriter("symbols.txt");
     root=new Node();
      root.setValue("");
      currParent=root;
      counter=0;
      walk();
      while(inputScan.hasNext())
      {
          
          if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)")||Objects.equals(token,"(tok_proc)"))
          {
         currParent.setValue("PROG");
         currParent.setNumber(counter);
         counter++;
         currParent.outputSymbol(symbols);
         parseProg();
          }
          else{
              //handle error
               System.out.println(word+" unexpected token found.");
                end();
          }
      }
      root.output(output, symbols, tree);
      symbols.close();
      tree.close();
      
    
    }
    
    public static void parseProg()throws Exception{
        if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
        {
            Node codeNode=new Node();
            codeNode.setValue("CODE");
            codeNode.setNumber(counter);
            counter++;
            codeNode.setParent(currParent);
            currParent.addChild(codeNode);
            currParent=codeNode;
            codeNode.outputSymbol(symbols);
            parseCode();
            currParent=currParent.getParent();
            //walk();
           // if(Objects.equals(token,"(tok_semico)"))
            //{   //System.out.println("OWO");
               // walk();
                if(Objects.equals(token,"(tok_proc)"))
                {
                    Node procNode=new Node();
                    procNode.setValue("PROC_DEFS");
                     procNode.setNumber(counter);
                    counter++;
                    procNode.setParent(currParent);
                    currParent.addChild(procNode);
                    currParent=procNode;
                    procNode.outputSymbol(symbols);
                    parseProc_defs();
                    if(currParent!=root)
                        currParent=currParent.getParent();
                    return;
                }/*else{
                    //handle error
                     System.out.println(word+" expected proc statement.");
                    end();
                }*/
           // }
        }
        else{
            
            //handle error
             System.out.println(word+" unexpected token found.");
            end();
            
        }
    }
    public static void parseCode()throws Exception
    {
        if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
        {
            Node instrNode=new Node();
            instrNode.setValue("INSTR");
            instrNode.setNumber(counter);
            counter++;
            instrNode.setParent(currParent);
            currParent.addChild(instrNode);
            currParent=instrNode;
            instrNode.outputSymbol(symbols);
            parseInstr();
            currParent=currParent.getParent();
           // walk();
           if(Objects.equals(token,"(tok_semico)")){
            semicoTest();
           }
        }
        else{
            
            //handle error
             System.out.println(word+" unexpected token foundl.");
            end();
            
        }
        
    }
    public static void parseCodePrime()throws Exception
    {
       if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
       {
           parseCode();
           walk();
           if(Objects.equals(token,"(tok_semico)"))
           {
               walk();
               if(Objects.equals(token,"(tok_proc)"))
               {
                   parseCode();
                   return;
               }else{
                   //handle error
                    System.out.println(word+" expected proc statement.");
                    end();
               }
           }else{
               //handle error
                System.out.println(word+" expected ;.");
                end();
           }
       }else{
           //handle error
            System.out.println(word+" unexpected token found.");
            end();
       }         
    }
    public static void parseProc_defs()throws Exception
    {
        if(Objects.equals(token,"(tok_proc)"))
        {
            Node procNode=new Node();
            procNode.setValue("PROC");
            procNode.setNumber(counter);
            counter++;
            procNode.setParent(currParent);
            currParent.addChild(procNode);
            currParent=procNode;
            procNode.outputSymbol(symbols);
            parseProc();
            currParent=currParent.getParent();
            //walk();
            if(Objects.equals(token,"(tok_proc)")){
            Node procDefsPrimeNode=new Node();
            procDefsPrimeNode.setValue("PROC_DEFSPRIME");
            procDefsPrimeNode.setNumber(counter);
            counter++;
            procDefsPrimeNode.setParent(currParent);
            currParent.addChild(procDefsPrimeNode);
            currParent=procDefsPrimeNode;
            procDefsPrimeNode.outputSymbol(symbols);
            parseProc_defsPrime();
            currParent=currParent.getParent();
            }
            return;
        }else
        {
            //handle error
             System.out.println(word+" expected proc statement.");
            end();
        }
    }
    public static void parseProc_defsPrime()throws Exception
    {
        if(Objects.equals(token,"(tok_proc)"))
        {
            Node procDefsNode=new Node();
            procDefsNode.setValue("PROC_DEFS");
            procDefsNode.setNumber(counter);
            counter++;
            procDefsNode.setParent(currParent);
            currParent.addChild(procDefsNode);
            currParent=procDefsNode;
            procDefsNode.outputSymbol(symbols);
            parseProc_defs();
            currParent=currParent.getParent();
        }
        counter--;
        return;
    }
    public static void parseInstr()throws Exception
    {
        String oldToken;
        String newToken;
        String oldWord;
        String newWord;
        if(Objects.equals(token,("(tok_halt)")))
        {
            Node haltNode=new Node();
            haltNode.setValue("halt");
            haltNode.setNumber(counter);
            counter++;
            haltNode.setParent(currParent);
            currParent.addChild(haltNode);
            haltNode.outputSymbol(symbols);
           
            walk();
            return;
        }
        if(Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)"))
        {
            Node declNode=new Node();
            declNode.setValue("DECL");
            declNode.setNumber(counter);
            counter++;
            declNode.setParent(currParent);
            currParent.addChild(declNode);
            currParent=declNode;
            declNode.outputSymbol(symbols);
            parseDecl();
            currParent=currParent.getParent();
            return;
        }
        if(Objects.equals(token,"(tok_input)")||Objects.equals(token,"(tok_output)"))
        {
            String ioType;
        if(Objects.equals(token,"(tok_input)"))
        ioType="input";
    else
        ioType="output";
            Node ioNode=new Node();
            ioNode.setValue("IO "+ioType);
            ioNode.setNumber(counter);
            counter++;
            ioNode.setParent(currParent);
            currParent.addChild(ioNode);
            currParent=ioNode;
            ioNode.outputSymbol(symbols);
            parseIO();
            currParent=currParent.getParent();
            return;
        }
        if(Objects.equals(token,"(tok_userdef)"))
        {
            oldToken=token;
            oldWord=word;
            walk();
            newToken=token;
            newWord=word;
            if(Objects.equals(token,"(tok_equals)"))
            {
                token=oldToken;
                word=oldWord;
                Node varNode=new Node();
            varNode.setValue("VAR");
            varNode.setNumber(counter);
            counter++;
            varNode.setParent(currParent);
            currParent.addChild(varNode);
            currParent=varNode;
            varNode.outputSymbol(symbols);
                parseVar();
                currParent=currParent.getParent();
                token=newToken;
                word=newWord;
                walk();
                 Node varEqNode=new Node();
            varEqNode.setValue("VAR_EQ");
            varEqNode.setNumber(counter);
            counter++;
            varEqNode.setParent(currParent);
            currParent.addChild(varEqNode);
            currParent=varEqNode;
            varEqNode.outputSymbol(symbols);
                parseVarEQ();
                currParent=currParent.getParent();
                //walk();
                return;
            }
            else
            {
                //if(Objects.equals(token,"(tok_semico)"))
               // {
                
                    Node callNode=new Node();
                    callNode.setValue("CALL");
                    callNode.setNumber(counter);
                    counter++;
                   callNode.setParent(currParent);
                   currParent.addChild(callNode);
                    currParent=callNode;
                    callNode.outputSymbol(symbols);
                    token=oldToken;
                    word=oldWord;
                    parseCall();
                    currParent=currParent.getParent();
                    token=newToken;
                    word=newWord;
                    return;
               // }else{
                //handle error
                 //System.out.println(word+" expected = or ;.");
                //end();
                //}
            }
        
            
        }
        if(Objects.equals(token,"(tok_if)"))
        {
             Node condNode=new Node();
            condNode.setValue("COND_BRANCH");
            condNode.setNumber(counter);
            counter++;
            condNode.setParent(currParent);
            currParent.addChild(condNode);
            currParent=condNode;
            condNode.outputSymbol(symbols);
            parseCond_Branch();
            currParent=currParent.getParent();
            return;
        }
        if(Objects.equals(token,"(tok_while)")||Objects.equals(token, "(tok_for)"))
        {
             Node loopNode=new Node();
            loopNode.setValue("COND_LOOP");
            loopNode.setNumber(counter);
            counter++;
            loopNode.setParent(currParent);
            currParent.addChild(loopNode);
            currParent=loopNode;
            loopNode.outputSymbol(symbols);
            parseCond_Loop();
            currParent=currParent.getParent();
            return;
        }
        else{
            //handle error
             System.out.println(word+" expected if,for or while statement.");
            end();
        }
    }
    public static void parseInstr_Code()throws Exception
    {
        if(Objects.equals(token,("(tok_semico)")))
        {
            walk();
             Node codeNode=new Node();
            codeNode.setValue("CODE");
            codeNode.setNumber(counter);
            counter++;
            codeNode.setParent(currParent);
            currParent.addChild(codeNode);
            currParent=codeNode;
            codeNode.outputSymbol(symbols);
            parseCode();
            currParent=currParent.getParent();
            return;
        }
       counter--;
        
    }
    public static void parseIO()throws Exception
    {if(Objects.equals(token,"(tok_input)")||Objects.equals(token,"(tok_output)"))
    { 
        walk();
        if(Objects.equals(token,"(tok_oparen)"))
        {
            walk();
            if(Objects.equals(token,"(tok_userdef)"))
            {
                Node varNode=new Node();
            varNode.setValue("VAR");
            varNode.setNumber(counter);
            counter++;
            varNode.setParent(currParent);
            currParent.addChild(varNode);
            currParent=varNode;
            varNode.outputSymbol(symbols);
                parseVar();
                currParent=currParent.getParent();
                walk();
                if(Objects.equals(token,"(tok_cparen)"))
                {
                    walk();
                    return;
                }else{
                    //handle error
                     System.out.println(word+" expected ).");
                        end();
                }
            }else{
                //handle error
                 System.out.println(word+" expected user defined statement.");
                end();
            }
        }else{
            //handle error
             System.out.println(word+" expected (.");
            end();
        }
        return;
    }
    else
    {
        //handle error
         System.out.println(word+" expected IO token.");
            end();
    }
        
        
    }
    public static void parseCall()throws Exception
    {
     if(Objects.equals(token,"(tok_userdef)"))
     {
         Node funcNode=new Node();
            funcNode.setValue("function "+word.substring(word.indexOf(":")+1));
            funcNode.setNumber(counter);
            counter++;
            funcNode.setParent(currParent);
            currParent.addChild(funcNode);
           // currParent=funcNode;
            funcNode.outputSymbol(symbols);
        
         return;
     }
     else
     {
         //handle error
          System.out.println(word+" expected user defined value.");
            end();
     }
    }
    public static void parseDecl()throws Exception
    {
        if(Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)"))
        {
            Node typeNode=new Node();
            typeNode.setValue("TYPE");
            typeNode.setNumber(counter);
            counter++;
            typeNode.setParent(currParent);
            currParent.addChild(typeNode);
            currParent=typeNode;
            typeNode.outputSymbol(symbols);
            parseType();
            currParent=currParent.getParent();
            walk();
            if(Objects.equals(token,"(tok_userdef)"))
            {
                Node nameNode=new Node();
                nameNode.setValue("NAME");
                nameNode.setNumber(counter);
                counter++;
                nameNode.setParent(currParent);
                currParent.addChild(nameNode);
                 currParent=nameNode;
                 nameNode.outputSymbol(symbols);
                parseName();
                currParent=currParent.getParent();
                walk();
                //parseRoll_Decl();
                return;
            }
            else{
                //handle error
                 System.out.println(word+" expected user defined value.");
                end();
            }
            return;
        }
        else
        {
            //handle error
             System.out.println(word+" expected type statement.");
            end();
        }
    }
    public static void parseRoll_Decl()throws Exception
    {
      if(Objects.equals(token,"(tok_semico)")){
         walk();
         if(Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")){
         Node declNode=new Node();
         declNode.setValue("DECL");
         declNode.setNumber(counter);
         counter++;
         declNode.setParent(currParent);
         currParent.addChild(declNode);
         currParent=declNode;
         declNode.outputSymbol(symbols);
         parseDecl();
         currParent=currParent.getParent();
         }
      }
      return;
    
        
    }
    public static void parseType()throws Exception
    {
        if(Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)"))
        {
            //handle output
            Node typeNode=new Node();
            typeNode.setValue("type "+word.substring(word.indexOf(":")+1));
            typeNode.setNumber(counter);
            counter++;
            typeNode.setParent(currParent);
            currParent.addChild(typeNode);
           // currParent=funcNode;
            typeNode.outputSymbol(symbols);
            return;
        }
                    else{
            //handle error
             System.out.println(word+" expected type statement.");
            end();
        }
    }
        
    
    public static void parseName()throws Exception
    {
       if(Objects.equals(token,"(tok_userdef)"))
       {Node nameNode=new Node();
            nameNode.setValue("name "+word.substring(word.indexOf(":")+1));
            nameNode.setNumber(counter);
            counter++;
            nameNode.setParent(currParent);
            currParent.addChild(nameNode);
           // currParent=funcNode;
            nameNode.outputSymbol(symbols);
           return;
       }
       else
       {
           //handle error
            System.out.println(word+" expected user defined value.");
            end();
       }
    }
    public static void parseVar()throws Exception
    {
        
    if(Objects.equals(token,"(tok_userdef)"))
       {
           Node varNode=new Node();
            varNode.setValue("variable "+word.substring(word.indexOf(":")+1));
            varNode.setNumber(counter);
            counter++;
            varNode.setParent(currParent);
            currParent.addChild(varNode);
           // currParent=funcNode;
            varNode.outputSymbol(symbols);
           return;
       }
       else
       {
           //handle error
            System.out.println(word+" expected user defined value.");
            end();
       }     
    
        
    }
    //not used
    public static void parseAssign()throws Exception
    {if(Objects.equals(token,"(tok_userdef)"))
    {
        Node varNode=new Node();
            varNode.setValue("VAR");
            varNode.setNumber(counter);
            counter++;
            varNode.setParent(currParent);
            currParent.addChild(varNode);
            currParent=varNode;
            varNode.outputSymbol(symbols);
        parseVar();
        currParent=currParent.getParent();
        walk(); 
         if(Objects.equals(token,"(tok_equals)"))
         {
          walk();
         if(Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_int)")||Objects.equals(token,"(tok_add)")||Objects.equals(token,"(tok_sub)")||Objects.equals(token,"(tok_mult)")||Objects.equals(token,"(tok_str)")||Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)"))
         {
             Node varEqNode=new Node();
            varEqNode.setValue("VAREQ");
            varEqNode.setNumber(counter);
            counter++;
            varEqNode.setParent(currParent);
            currParent.addChild(varEqNode);
            currParent=varEqNode;
            varEqNode.outputSymbol(symbols);
           parseVarEQ();
           currParent=currParent.getParent();
           return;
         }
         else
         {
             //handle error
              System.out.println(word+" unexpected token found.");
                end();
         }
    }else{
        //handle error
         System.out.println(word+" expected =.");
            end();
    }
    }
    else{
        //handle error
         System.out.println(word+" expected user defined value.");
            end();
    }
    }
        
        
    
    public static void parseVarEQ()throws Exception
    {if(Objects.equals(token,"(tok_userdef)"))
    {
        Node varNode=new Node();
            varNode.setValue("VAR");
            varNode.setNumber(counter);
            counter++;
            varNode.setParent(currParent);
            currParent.addChild(varNode);
            currParent=varNode;
            varNode.outputSymbol(symbols);
       parseVar();
       currParent=currParent.getParent();
       walk();
       return;
    }
    if(Objects.equals(token,"(tok_int)")||Objects.equals(token,"(tok_add)")||Objects.equals(token,"(tok_sub)")||Objects.equals(token,"(tok_mult)"))
    {
        Node numExprNode=new Node();
            numExprNode.setValue("NUMEXPR");
            numExprNode.setNumber(counter);
            counter++;
            numExprNode.setParent(currParent);
            currParent.addChild(numExprNode);
            currParent=numExprNode;
            numExprNode.outputSymbol(symbols);
        parseNumexpr();
        currParent=currParent.getParent();
        return;
    }
    if(Objects.equals(token,"(tok_str)"))
    {Node stringNode=new Node();
            stringNode.setValue("stringLit "+word.substring(word.indexOf(":")+1));
            stringNode.setNumber(counter);
            counter++;
            stringNode.setParent(currParent);
            currParent.addChild(stringNode);
            //currParent=varNode;
            stringNode.outputSymbol(symbols);
            walk();
        return;
        
    }
    if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)"))
    {
        Node boolNode=new Node();
            boolNode.setValue("BOOL");
            boolNode.setNumber(counter);
            counter++;
            boolNode.setParent(currParent);
            currParent.addChild(boolNode);
            currParent=boolNode;
            boolNode.outputSymbol(symbols);
        parseBool();
        currParent=currParent.getParent();
        return;
    }
    else
    {
        //handle error
         System.out.println(word+" expected boolean, math or user defined value.");
            end();
    }
    }
    public static void parseNumexpr()throws Exception
    {
        if(Objects.equals(token,"(tok_int)"))
        {
            Node intNode=new Node();
            intNode.setValue("integer "+word.substring(word.indexOf(":")+1));
            intNode.setNumber(counter);
            counter++;
            intNode.setParent(currParent);
            currParent.addChild(intNode);
            //currParent=varNode;
            intNode.outputSymbol(symbols);
            walk();
            return;
        }
        if(Objects.equals(token,"(tok_userdef)"))
        {
           Node varNode=new Node();
            varNode.setValue("VAR");
            varNode.setNumber(counter);
            counter++;
            varNode.setParent(currParent);
            currParent.addChild(varNode);
            currParent=varNode;
            varNode.outputSymbol(symbols);
            parseVar();
            currParent=currParent.getParent();
            walk();
            return;
        }
        if(Objects.equals(token,"(tok_add)")||Objects.equals(token,"(tok_sub)")||Objects.equals(token,"(tok_mult)"))
        {
            String val="CALC";
            if(Objects.equals(token,"(tok_add)"))
            {
                val="CALCADD";
            }
            if(Objects.equals(token,"(tok_sub)"))
            {
                val="CALCSUB";
            }
            if(Objects.equals(token,"(tok_mult)"))
            {
                val="CALCMULT";
            }
           Node calcNode=new Node();
            calcNode.setValue(val);
            calcNode.setNumber(counter);
            counter++;
            calcNode.setParent(currParent);
            currParent.addChild(calcNode);
            currParent=calcNode;
            calcNode.outputSymbol(symbols);
            parseCalc();
            currParent=currParent.getParent();
            return;
        }
        else
        {
            //handle error
             System.out.println(word+" expected math statement or numerical variable.");
            end();
        }
        
    }
    public static void parseCalc()throws Exception
    {
        if(Objects.equals(token,"(tok_add)")||Objects.equals(token,"(tok_sub)")||Objects.equals(token,"(tok_mult)"))
        {
            walk();
            if(Objects.equals(token,"(tok_oparen)"))
            {
               walk();
               if(Objects.equals(token,"(tok_int)")||Objects.equals(token,"(tok_add)")||Objects.equals(token,"(tok_sub)")||Objects.equals(token,"(tok_mult)")||Objects.equals(token,"(tok_userdef)"))
               {
                   Node numExprNode=new Node();
                    numExprNode.setValue("NUMEXPR");
                    numExprNode.setNumber(counter);
                    counter++;
                    numExprNode.setParent(currParent);
                    currParent.addChild(numExprNode);
                    currParent=numExprNode;
                    numExprNode.outputSymbol(symbols);
                   parseNumexpr();
                   currParent=currParent.getParent();
                  // walk();
                   if(Objects.equals(token,"(tok_comma)"))
                   {
                       walk();
                       if(Objects.equals(token,"(tok_int)")||Objects.equals(token,"(tok_add)")||Objects.equals(token,"(tok_sub)")||Objects.equals(token,"(tok_mult)")||Objects.equals(token,"(tok_userdef)"))
                       {
                           Node numExprNode2=new Node();
                            numExprNode2.setValue("NUMEXPR");
                            numExprNode2.setNumber(counter);
                            counter++;
                            numExprNode2.setParent(currParent);
                            currParent.addChild(numExprNode2);
                            currParent=numExprNode2;
                            numExprNode2.outputSymbol(symbols);
                           parseNumexpr();
                           currParent=currParent.getParent();
                           //walk();
                           if(Objects.equals(token,"(tok_cparen)"))
                           {
                               walk();
                               return;
                           }
                           else{
                               //handle error
                                System.out.println(word+" expected ).");
                                end();
                           }
                       }
                       else
                       {
                           //handle error
                            System.out.println(word+" expected math statement.");
                            end();
                       }
                   }
                   else
                   {
                       //handle error
                        System.out.println(word+" expected ,.");
                        end();
                   }
               }
               else
               {
                   //handle error
                    System.out.println(word+" expected math statement.");
                    end();
               }
            }
            else
            {
                //handle error
                 System.out.println(word+" expected (.");
                 end();
            }
            
        }else
        {
            //handle error
             System.out.println(word+" expected math statement.");
            end();
        }
        
    }
    public static void parseCond_Branch()throws Exception
    {
       if(Objects.equals(token,"(tok_if)"))
       {
          // System.out.println("OWO");
           walk();
           if(Objects.equals(token,"(tok_oparen)"))
           {
               walk();
               if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)")||Objects.equals(token,"(tok_userdef)"))
               {
                   Node boolNode=new Node();
                    boolNode.setValue("BOOL");
                    boolNode.setNumber(counter);
                    counter++;
                    boolNode.setParent(currParent);
                    currParent.addChild(boolNode);
                    currParent=boolNode;
                    boolNode.outputSymbol(symbols);
                   parseBool();
                   currParent=currParent.getParent();
                   //walk();
                   if(Objects.equals(token,"(tok_cparen)"))
                   {
                       walk();
                       if(Objects.equals(token,"(tok_then)"))
                       {
                           walk();
                           if(Objects.equals(token,"(tok_obrace)"))
                           { 
                               walk();
                               if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
                               {
                                   Node codeNode=new Node();
                                    codeNode.setValue("CODE");
                                    codeNode.setNumber(counter);
                                    counter++;
                                    codeNode.setParent(currParent);
                                    currParent.addChild(codeNode);
                                    currParent=codeNode;
                                    codeNode.outputSymbol(symbols);
                                   parseCode();
                                   currParent=currParent.getParent();
                                   //walk();
                                   if(Objects.equals(token,"(tok_cbrace)"))
                                   {
                                       walk();
                                       if(Objects.equals(token,"(tok_else)"))
                                       {
                                           Node elseNode=new Node();
                                            elseNode.setValue("ELSE_BRANCH");
                                            elseNode.setNumber(counter);
                                            counter++;
                                             elseNode.setParent(currParent);
                                             currParent.addChild(elseNode);
                                            currParent=elseNode;
                                            elseNode.outputSymbol(symbols);
                                           parseElse_Branch();
                                           currParent=currParent.getParent();
                                       }
                                       return;
                                   }else{
                                       //handle error
                                        System.out.println(word+" expected }.");
                                        end();
                                   }
                               }
                               else{
                                   //handle error
                                    System.out.println(word+" unexpected token found.");
                                    end();
                               }
                           }else
                           {
                               //handle error
                                System.out.println(word+" expected {.");
                                end();
                           }
                       }
                       else{
                           //handle error
                            System.out.println(word+" expected then statement.");
                            end();
                       }
                   }else
                   {
                       //handle error
                        System.out.println(word+" expected ).");
                        end();
                   }
               }
               else
               {
                   //handle error
                    System.out.println(word+" expected boolean statement.");
                    end();
               }
           }
           else
           {
               //handle error
                System.out.println(word+" expected (.");
                end();
           }
       }else
       {
           //handle error
            System.out.println(word+" expected if statement.");
            end();
       }
    }
    public static void parseElse_Branch()throws Exception
    {
        walk();
        if(Objects.equals(token,"(tok_obrace)"))
        {
            walk();
            if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
            {
                Node codeNode=new Node();
                    codeNode.setValue("CODE");
                    codeNode.setNumber(counter);
                    counter++;
                    codeNode.setParent(currParent);
                    currParent.addChild(codeNode);
                    currParent=codeNode;
                    codeNode.outputSymbol(symbols);
                parseCode();
                currParent=currParent.getParent();
                //walk();
                if(Objects.equals(token,"(tok_cbrace)"))
                {
                    walk();
                    
                    return;
                }else{
                    //handle error
                     System.out.println(word+" expected }.");
                        end();
                }
            }else{
                //handle error
                 System.out.println(word+" unexpected token found.");
                  end();
            }
        }else{
            //handle error
             System.out.println(word+" expected {.");
            end();
        }
    }
    public static void parseBool()throws Exception
    {
        if(Objects.equals(token,"(tok_F)")||Objects.equals(token,"(tok_T)"))
        {Node TFNode=new Node();
                    TFNode.setValue("boolean "+word.substring(word.indexOf(":")+1));
                    TFNode.setNumber(counter);
                    counter++;
                    TFNode.setParent(currParent);
                    currParent.addChild(TFNode);
                    //currParent=TFNode;
                    TFNode.outputSymbol(symbols);
                    walk();
           return ;
        }
        if(Objects.equals(token,"(tok_userdef)"))
        {Node varNode=new Node();
                   varNode.setValue("VAR");
                    varNode.setNumber(counter);
                    counter++;
                    varNode.setParent(currParent);
                    currParent.addChild(varNode);
                    currParent=varNode;
                    varNode.outputSymbol(symbols);
            parseVar();
            currParent=currParent.getParent();
            walk();
            return;
        }
        if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)"))
        {
            String val="BOOLSET";
            if(Objects.equals(token,"(tok_eq)"))
            val="BOOLSETEQ"; 
            if(Objects.equals(token,"(tok_or)"))
                val="BOOLSETOR";
            if(Objects.equals(token,"(tok_and)"))
                val="BOOLSETAND";
            walk();
            Node boolSetNode=new Node();
                    boolSetNode.setValue(val);
                    boolSetNode.setNumber(counter);
                    counter++;
                    boolSetNode.setParent(currParent);
                    currParent.addChild(boolSetNode);
                    currParent=boolSetNode;
                    boolSetNode.outputSymbol(symbols);
            parseBoolSet();
            currParent=currParent.getParent();
            return;
        }
        if(Objects.equals(token, "(tok_not)"))
        {
            //will need to add new node here for code gen
           walk();
           
               if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)")||Objects.equals(token,"(tok_userdef)"))
               {
                   Node boolNode=new Node();
                        boolNode.setValue("BOOLNOT");
                        boolNode.setNumber(counter);
                        counter++;
                        boolNode.setParent(currParent);
                        currParent.addChild(boolNode);
                        currParent=boolNode;
                        boolNode.outputSymbol(symbols);
                   parseBool();
                   currParent=currParent.getParent();
                   
                   
                   
               }else
               {
                   System.out.println(word+" expected boolean statement.");
                end();
               }
               
          
            
        }
        if(Objects.equals(token,"(tok_oparen)"))
        {
            walk();
            if(Objects.equals(token,"(tok_userdef)"))
            {Node varNode2=new Node();
                    varNode2.setValue("VAR");
                    varNode2.setNumber(counter);
                    counter++;
                    varNode2.setParent(currParent);
                    currParent.addChild(varNode2);
                    currParent=varNode2;
                    varNode2.outputSymbol(symbols);
                parseVar();
                currParent=currParent.getParent();
                walk();
                String val="LG_THAN";
                if(Objects.equals(token,"(tok_gthan)"))
                    val="GTHAN";
                if(Objects.equals(token,"(tok_lthan)"))
                    val="LTHAN";
                Node lgNode=new Node();
                    lgNode.setValue(val);
                    lgNode.setNumber(counter);
                    counter++;
                    lgNode.setParent(currParent);
                    currParent.addChild(lgNode);
                    currParent=lgNode;
                    lgNode.outputSymbol(symbols);
                parseLG_than();
                currParent=currParent.getParent();
                return;
            }else
            {
                //handle error
                 System.out.println(word+" expected user defined value or boolean statement.");
                end();
            }
            
        }
        
    }
    public static void parseLG_than()throws Exception
    {
        if(Objects.equals(token,"(tok_gthan)")||Objects.equals(token,"(tok_lthan)"))
        {
            walk();
            if(Objects.equals(token,"(tok_userdef)"))
            {
                Node varNode=new Node();
                    varNode.setValue("VAR");
                    varNode.setNumber(counter);
                    counter++;
                    varNode.setParent(currParent);
                    currParent.addChild(varNode);
                    currParent=varNode;
                    varNode.outputSymbol(symbols);
                parseVar();
                currParent=currParent.getParent();
                walk();
                if(Objects.equals(token,"(tok_cparen)"))
                {
                    walk();
                    return;
                }else{
                    //handle error
                     System.out.println(word+" expected ).");
                        end();
                }
            }else
            {
                //handle error
                 System.out.println(word+" expected user defined value.");
                end();
            }
        }else
        {
            //handle error
             System.out.println(word+" expected < or >.");
            end();
        }
        
    }
    public static void parseCond_Loop()throws Exception
    {
       if(Objects.equals(token,"(tok_while)"))
       {
           walk();
           if(Objects.equals(token,"(tok_oparen)"))
           {
               walk();
               if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)")||Objects.equals(token,"(tok_userdef)"))
               {
                   Node boolNode=new Node();
                    boolNode.setValue("BOOL");
                    boolNode.setNumber(counter);
                    counter++;
                    boolNode.setParent(currParent);
                    currParent.addChild(boolNode);
                    currParent=boolNode;
                    boolNode.outputSymbol(symbols);
                   parseBool();
                   currParent=currParent.getParent();
                  // walk();
                   if(Objects.equals(token,"(tok_cparen)"))
                   {
                       walk();
                       if(Objects.equals(token,"(tok_obrace)"))
                       {
                           walk();
                           if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
                           {
                               Node codeNode=new Node();
                                codeNode.setValue("CODE");
                                codeNode.setNumber(counter);
                                counter++;
                                codeNode.setParent(currParent);
                                currParent.addChild(codeNode);
                                currParent=codeNode;
                                codeNode.outputSymbol(symbols);
                             parseCode();
                             currParent=currParent.getParent();
                             //walk();
                             if(Objects.equals(token,"(tok_cbrace)"))
                             {//handle output
                                 walk();
                                 return;
                             }else{
                                 //handle error
                                  System.out.println(word+" expected }.");
                                    end();
                             }
                           }else{
                               //handle error
                                System.out.println(word+" unexpected token found.");
                                end();
                           }
                       }else
                       {
                           //handle error
                            System.out.println(word+" expected {.");
                            end();
                       }
                   }else{
                       //handle error
                        System.out.println(word+" expected ).");
                        end();
                   }
               }else
               {
                   //handle error
                    System.out.println(word+" expected boolean statement.");
                    end();
               }
           }else
           {
               //handle error
                System.out.println(word+" expected (.");
                end();
           }
       }
       if(Objects.equals(token,"(tok_for)"))
       {
          walk();
          if(Objects.equals(token,"(tok_oparen)"))
          {
              walk();
              if(Objects.equals(token,"(tok_userdef)"))
              {
                  Node initvarNode=new Node();
                    initvarNode.setValue("VAR");
                    initvarNode.setNumber(counter);
                    counter++;
                    initvarNode.setParent(currParent);
                    currParent.addChild(initvarNode);
                    currParent=initvarNode;
                    initvarNode.outputSymbol(symbols);
                  parseVar();
                  currParent=currParent.getParent();
                  walk();
                  if(Objects.equals(token,"(tok_equals)"))
                  {
                      walk();
                      if(Objects.equals(token,"(tok_int)") && Objects.equals(word.substring(word.length()-1),"0"))
                      {
                          walk();
                          if(Objects.equals(token,"(tok_semico)"))
                          {
                              walk();
                              if(Objects.equals(token,"(tok_userdef)"))
                              {
                                  Node gatevarNode=new Node();
                                    gatevarNode.setValue("VAR");
                                    gatevarNode.setNumber(counter);
                                    counter++;
                                    gatevarNode.setParent(currParent);
                                    currParent.addChild(gatevarNode);
                                    currParent=gatevarNode;
                                    gatevarNode.outputSymbol(symbols);
                                  parseVar();
                                  currParent=currParent.getParent();
                                  walk();
                                  if(Objects.equals(token,"(tok_lthan)"))
                                  {
                                    walk();
                                    if(Objects.equals(token,"(tok_userdef)"))
                                    {
                                        Node checkvarNode=new Node();
                                        checkvarNode.setValue("VAR");
                                        checkvarNode.setNumber(counter);
                                        counter++;
                                        checkvarNode.setParent(currParent);
                                        currParent.addChild(checkvarNode);
                                        currParent=checkvarNode;
                                        checkvarNode.outputSymbol(symbols);
                                        parseVar();
                                        currParent=currParent.getParent();
                                        walk();
                                        if(Objects.equals(token,"(tok_semico)"))
                                        {
                                            walk();
                                            if(Objects.equals(token,"(tok_userdef)"))
                                            {
                                                Node changevarNode=new Node();
                                                 changevarNode.setValue("VAR");
                                                changevarNode.setNumber(counter);
                                                counter++;
                                                changevarNode.setParent(currParent);
                                                currParent.addChild(changevarNode);
                                                currParent=changevarNode;
                                                changevarNode.outputSymbol(symbols);
                                                parseVar();
                                                currParent=currParent.getParent();
                                               walk();
                                               if(Objects.equals(token,"(tok_equals)"))
                                               {
                                                   walk();
                                                   if(Objects.equals(token,"(tok_add)"))
                                                   {
                                                       Node calcNode=new Node();
                                                        calcNode.setValue("CALCADD");
                                                        calcNode.setNumber(counter);
                                                        counter++;
                                                        calcNode.setParent(currParent);
                                                        currParent.addChild(calcNode);
                                                        currParent=calcNode;
                                                        calcNode.outputSymbol(symbols);
                                                       parseCalc();
                                                       currParent=currParent.getParent();
                                                       //walk();
                                                       if(Objects.equals(token,"(tok_cparen)"))
                                                       {
                                                           walk();
                                                           if(Objects.equals(token,"(tok_obrace)"))
                                                           {
                                                               walk();
                                                               if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
                                                               {
                                                                   Node codeNode=new Node();
                                                                    codeNode.setValue("CODE");
                                                                    codeNode.setNumber(counter);
                                                                    counter++;
                                                                    codeNode.setParent(currParent);
                                                                    currParent.addChild(codeNode);
                                                                    currParent=codeNode;
                                                                    codeNode.outputSymbol(symbols);
                                                                   parseCode();
                                                                   currParent=currParent.getParent();
                                                                   //walk();
                                                                   if(Objects.equals(token,"(tok_cbrace)"))
                                                                   {
                                                                       walk();
                                                                       //handle output
                                                                       return;
                                                                   }else{
                                                                       //handle error
                                                                        System.out.println(word+" expected }.");
                                                                        end();
                                                                   }
                                                               }else{
                                                                   //handle error
                                                                    System.out.println(word+" unexpected token.");
                                                                    end();
                                                               }
                                                           }else{
                                                               //handle error
                                                                System.out.println(word+" expected {.");
                                                                end();
                                                           }
                                                       }else{
                                                           //handle error
                                                            System.out.println(word+" expected ).");
                                                            end();
                                                       }
                                                   }else{
                                                       //handle error
                                                        System.out.println(word+" expected add statement.");
                                                        end();
                                                   }
                                               }else{
                                                   //handle error
                                                    System.out.println(word+" expected =.");
                                                    end();
                                               }
                                            }else{
                                                //handle error
                                                 System.out.println(word+" expected user defined value.");
                                                   end();
                                            }
                                        }else{
                                            //handle error
                                             System.out.println(word+" expected ;.");
                                                end();
                                        }
                                    }else{
                                        //handle error
                                         System.out.println(word+" expected user defined value.");
                                            end();
                                    }
                                  }else{
                                      //handle error
                                       System.out.println(word+" expected =.");
                                        end();
                                  }
                              }else{
                                  //handle error
                                   System.out.println(word+" expected user defined value.");
                                    end();
                              }
                          }else{
                              //handle error
                               System.out.println(word+" expected ;.");
                                end();
                          }
                      }else{
                          //handle error
                           System.out.println(word+" expected 0.");
                            end();
                      }
                  }else
                  {
                      //handle error
                       System.out.println(word+" expected =.");
                        end();
                  }
              }else{
                  //handle error
                   System.out.println(word+" expected user defined value.");
                    end();
              }
          }else{
              //handle error
               System.out.println(word+" expected (.");
                end();
          }
       }else{
           //handle error
            System.out.println(word+" expected for statement.");
            end();
       }
    }
    public static void parseProc()throws Exception
    {
        if(Objects.equals(token,"(tok_proc)"))
        {
            walk();
            if(Objects.equals(token,"(tok_userdef)"))
            {
                Node procNode=new Node();
                procNode.setValue("proc "+word.substring(word.indexOf(":")+1));
                procNode.setNumber(counter);
                counter++;
                procNode.setParent(currParent);
                currParent.addChild(procNode);
                procNode.outputSymbol(symbols);
                walk();
                if(Objects.equals(token,"(tok_obrace)"))
                {
                    walk();
                    if(Objects.equals(token, "(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)")||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)"))
                    {
                        Node progNode=new Node();
                        progNode.setValue("PROG");
                        progNode.setNumber(counter);
                        counter++;
                        progNode.setParent(currParent);
                        currParent.addChild(progNode);
                        currParent=progNode;
                        progNode.outputSymbol(symbols);
                        parseProg();
                        currParent=currParent.getParent();
                        //walk();
                        if(Objects.equals(token,"(tok_cbrace)"))
                        {
                            
                           walk(); 
                            return;
                        }else{
                            //handle error
                             System.out.println(word+" expected }.");
                             end();
                        }
                    }else{
                        //handle error
                         System.out.println(word+" invalid token.");
                         end();
                    }
                }else{
                    //handle error
                     System.out.println(word+" expected {.");
                     end();
                }
            }else{
                //handle error
                 System.out.println(word+" expected user-defined value.");
                end();
            }
        }else{
            //handle error
            System.out.println(word+" expected proc statement.");
            end();
        }
        
    }
    public static void parseBoolSet()throws Exception
    {
        if(Objects.equals(token,"(tok_oparen)"))
            {
                walk();
                if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)")||Objects.equals(token,"(tok_userdef)"))
                {
                    if(token.equals("(tok_userdef)"))
                    {
                      Node var1Node=new Node();
                      var1Node.setValue("VAR");
                      var1Node.setNumber(counter);
                      counter++;
                      var1Node.setParent(currParent);
                      currParent.addChild(var1Node);
                      currParent=var1Node;
                      var1Node.outputSymbol(symbols);
                      parseVar();
                      currParent=currParent.getParent();
                      walk();
                    }else{
                    Node boolNode=new Node();
                        boolNode.setValue("BOOL");
                        boolNode.setNumber(counter);
                        counter++;
                        boolNode.setParent(currParent);
                        currParent.addChild(boolNode);
                        currParent=boolNode;
                        boolNode.outputSymbol(symbols);
                   parseBool();
                   currParent=currParent.getParent();
                    }
                   //walk();
                   if(Objects.equals(token,"(tok_comma)"))
                   {
                       walk();
                       if(Objects.equals(token,"(tok_eq)")||Objects.equals(token,"(tok_oparen)")||Objects.equals(token,"(tok_not)")||Objects.equals(token,"(tok_and)")||Objects.equals(token,"(tok_or)")||Objects.equals(token,"(tok_T)")||Objects.equals(token,"(tok_F)")||Objects.equals(token,"(tok_userdef)"))
                       {
                           if(token.equals("(tok_userdef)"))
                            {
                                Node var2Node=new Node();
                                var2Node.setValue("VAR");
                                var2Node.setNumber(counter);
                                counter++;
                                var2Node.setParent(currParent);
                                currParent.addChild(var2Node);
                                currParent=var2Node;
                                var2Node.outputSymbol(symbols);
                                parseVar();
                                currParent=currParent.getParent();
                                walk();
                            }else{
                           
                           Node boolNode2=new Node();
                        boolNode2.setValue("BOOL");
                        boolNode2.setNumber(counter);
                        counter++;
                        boolNode2.setParent(currParent);
                        currParent.addChild(boolNode2);
                        currParent=boolNode2;
                        boolNode2.outputSymbol(symbols);
                           parseBool();
                           currParent=currParent.getParent();
                           //walk();
                           }
                           if(Objects.equals(token,"(tok_cparen)"))
                           {
                               walk();
                               return;
                           }else
                           {
                               //handle error
                               System.out.println(word+" } expected.");
                               end();
                           }
                       }else
                       {
                           //handle error
                           System.out.println(word+ " boolean expression or variable expected.");
                           end();
                       }
                   }else{
                       //handle error
                       System.out.println(word+" comma expected");
                       end();
                   }
                }else
                {
                    //handle error
                    System.out.println(word+ " boolean expression or variable expected.");
                    end();
                }
            }else{
                //handle error
                System.out.println(word+" ( expected.");
                end();
            }
    }
    public static void semicoTest() throws Exception
    {
       walk();
       if(Objects.equals(token,"(tok_halt)")||Objects.equals(token,"(tok_num)")||Objects.equals(token,"(tok_string)")||Objects.equals(token,"(tok_bool)")||Objects.equals(token,"(tok_input)" )||Objects.equals(token,"(tok_output)")||Objects.equals(token,"(tok_userdef)")||Objects.equals(token,"(tok_if)")||Objects.equals(token,"(tok_while)")||Objects.equals(token,"(tok_for)")){
           Node instrCodeNode=new Node();
            instrCodeNode.setValue("CODE");
            instrCodeNode.setNumber(counter);
            counter++;
            instrCodeNode.setParent(currParent);
            currParent.addChild(instrCodeNode);
            currParent=instrCodeNode;
            instrCodeNode.outputSymbol(symbols);
            parseCode();
            currParent=currParent.getParent();
            return;
       }
      /* if(Objects.equals(token,"(tok_proc)"))
       {
           Node procNode=new Node();
            procNode.setValue("PROC");
             procNode.setNumber(counter);
             counter++;
             procNode.setParent(currParent);
             currParent.addChild(procNode);
             currParent=procNode;
             procNode.outputSymbol(symbols);
             parseProc();
                    
            currParent=currParent.getParent();
           
       }*/
    }
    public static void walk()
    {
        if(inputScan.hasNext()){
        word=inputScan.next();
         token=inputScan.next(); 
         if(!token.startsWith("("))
                 {
                    word=word+" "+token;
                    token=inputScan.next();
                 }
        }
        else 
            System.out.println("ran out of tokens/eof reached if only 1");
    }

    public static void end()throws Exception
    {
       System.out.println(token);
       System.out.println(word);
         symbols.close();
        System.exit(0);
    }
    
    
    
    public static void lex() throws Exception
    {
        BufferedReader reader=new BufferedReader(new InputStreamReader(System.in)); 
     String filename=reader.readLine();
     File outFile=new File("output.txt");
     outFile.createNewFile();
    writer=new FileWriter(outFile);
     
    ArrayList buffer=new ArrayList();
     FileReader filereader=new FileReader(filename);
     int x;
     while((x=filereader.read())!=-1)
     {
         column++;
         if((char)x=='\n')
         {
             column=1;
             row++;
         }
         symbol=false;
         buffer.add((char)x);
         
         //for string literals
         
         if((char)x=='"' && quote==false)
         {
             //quote=true;
             stringLit=true;
         }
        if(stringLit){
        if((char)x=='"' && quote==true && quoteCount<=8)
         {
                 //output lastToken
             outputToFile();
             quote=false;
             stringLit=false;
             quoteCount=0;
             lastToken="";
             symbol=true;
         }
         else
                 if(quote==true && quoteCount>8)
             {
                 //string length error
                 //outFile.delete();
                System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+lastToken+" String literal too long(max 8 characters).");
                writer.close();
                 return;
             }
        if(stringLit)
            quote=true;
        }
         if(stringLit && (char)x!='"' && (char)x!='\n' && stringLiteralCheck((char)x))
         {
             quoteCount++;
             lastToken=lastToken+(char)x;
         }
         else if (stringLit && (char)x!='"')
         {
             //handle incorrect character error
             //outFile.delete();
             stringLit=false;
             quote=false;
             quoteCount=0;
            System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+(char)x+" is not a valid string literal character.");
            writer.close();
            return;
         }
         
         
         /*if(!stringLit && (char)x==' ')
         {
             if(lastToken=="")
             //output last token and clear buffer
         }*/
         
         
         //for int literals
         if(!stringLit && !userDef)
         {
             if(intLit==false && intLiteralCheck((char)x))
                 intLit=true;
             if(intLit){
             if((char)x=='-' && negate==false && Objects.equals(lastToken,""))
                 negate=true;
             else
             {
                 //handle negate error
                 if((negate && (char)x=='-')||(!Objects.equals(lastToken,"")&&(char)x=='-')||(Objects.equals(lastToken,"")&&(char)x=='-')){
                 //outFile.delete();
                 System.out.println("Lexical Error [line: "+row+", col: "+column+"]: Invalid negation of integer literal.");
                 negate=false;
                 writer.close();
                 return;}
             }
             if(!intLiteralCheck((char)x))
             {
                 if(symbolCheck((char)x))
                 {
                     outputToFile();
                     lastToken="";
                     lastToken=lastToken+(char)x;
                     intLit=false;
                     outputToFile();
                     lastToken="";
                     symbol=true;
                     negate=false;
                 }
                 else
                     if(Character.isWhitespace((char)x))
                     {
                         outputToFile();
                         lastToken="";
                         intLit=false;
                         negate=false;
                     }else{
                         //handle int error
                        // outFile.delete();
                         System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+(char)x+" is not a valid integer literal character.");
                         negate=false;
                         writer.close();
                         return;
                     }
                     
                 
             }
             else
                 lastToken=lastToken+(char)x;
             if((char)x=='0'&& Objects.equals(lastToken,"0"))
             {
                 //lastToken="0";
                 zero=true;
             }
            // else
                // if(!zero && (char)x=='0')
           //  { 
               //  lastToken=lastToken+(char)x;
           //  }else if((char)x=='0')
                // {
                //     //handle zero error
                    // outFile.delete();
                    // System.out.println("Lexical Error [line: "+row+", col: "+column+"]: Invalid 0 position in integer literal.");
                    // writer.close();
                    // return;
                // }
            }
         }
         
         
         //for keywords/user defined variables
         
         if(!stringLit && !intLit &&!symbol)
         {
           if(userDef==false && Character.isLetter((char)x))
               userDef=true;
           if(userDef){
          /* else{
               if(Character.isWhitespace((char)x)||symbolCheck((char)x))
               {}
               else
               {
                   outFile.delete();
                   System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+(char)x+" is not a valid user defined value character.");
                   return;
               }
               //number error
           }*/
           
           if(Character.isLetter((char)x)||Character.isDigit((char)x))
               lastToken=lastToken+(char)x;
           else {
               if(Character.isWhitespace((char)x)||symbolCheck((char)x))
                       {
                           if(!lastToken.equals(lastToken.toLowerCase()))
                           {
                               if(keywordCheck())
                               {
                                    keyword=true;
                               outputToFile();
                               keyword=false;
                               userDef=false;
                               lastToken="";
                               }
                               else
                               {
                                 System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+(char)x+"Capital letters are not allowed in user defined variables.");
                                   writer.close();
                                     return;  
                               }
                           }
                           else
                           if(keywordCheck())
                           {
                               keyword=true;
                               outputToFile();
                               keyword=false;
                               userDef=false;
                               lastToken="";
                           }
                               //handle keyword output
                               else
                           {
                               outputToFile();
                               userDef=false;
                               lastToken="";
                           }
                               //handle user defined output
                               
                               if(symbolCheck((char)x))
                               {
                                   lastToken=lastToken+(char)x;
                                   outputToFile();
                                   lastToken="";
                                   symbol=true;
                               }
                       }
               else
               {
               //handle symbol error
                  // outFile.delete();
                   System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+(char)x+" is not a valid user defined value character.");
                   writer.close();
                   return;
               }
           }
           
         }}
         
         
         //for symbols
         if(!stringLit && !userDef && !intLit)
         {
             if(symbolCheck((char)x) && Objects.equals(lastToken, "") && !symbol)
             {
                 lastToken=lastToken+(char)x;
                 outputToFile();
                 lastToken="";
                 //write to file
             }
             else
             {if(Character.isWhitespace((char)x)||symbol)
             {}
             else
             {
                // outFile.delete();
                   System.out.println("Lexical Error [line: "+row+", col: "+column+"]: "+(char)x+" is not a valid symbol or element.");
                   writer.close();
                   return;
                 
             }
             }//handle error if you can think of any
         }
         
         if(Character.isWhitespace((char)x) && Objects.equals(lastToken, ""))
         {//nothing happens we ignore it
             
         }
       //dfa code here  
     }
     if(userDef)
     {
         if(keywordCheck())
         {
             keyword=true;
             outputToFile();
             keyword=false;
             userDef=false;
             lastToken="";
         }else{
         //output last token
         outputToFile();
         lastToken="";
         userDef=false;
         }
     }
     else
     {
         if(intLit)
         {
             outputToFile();
             lastToken="";
             intLit=false;
         }else
            if(!Objects.equals(lastToken, ""))
            {
               // outFile.delete();
                System.out.println("Error: Unexpected end of file.");
            }
             //handle error
     }
     filereader.close();
     writer.close();
        
    }
    
    public static boolean symbolCheck(char a)
    {
        for( int i=0; i<9; i++)
        {
            if(a==symbolKey[i])
                return true;
        }
        return false;
    }
    public static boolean keywordCheck()
    {
        for (int i=0; i<21; i++)
        {
            if(Objects.equals(lastToken,keywords[i]))
                return true;
        }
        return false;
    }
    public static boolean stringLiteralCheck(char a)
    {
       
        if((Character.isLetter(a) && Character.isLowerCase(a) )|| Character.isDigit(a) || Character.isWhitespace(a) )
        {
            return true;
        }
        //handle string literal error
        //quote=false;
        //stringLit=false;
        //quoteCount=0;
        return false;
    }
    public static boolean intLiteralCheck(char a)
    {
        if(Character.isDigit(a)|| a=='-')
            return true;
        
        
        return false;
        
    }
    public static void outputToFile()throws Exception
    {
        tokenCount++;
        writer.write(tokenCount+":");
      if(stringLit)
      {
         writer.write("\""+lastToken+"\" (tok_str)\n");
         return;
      }
      if(intLit)
      {
          writer.write(lastToken+" (tok_int)\n");
          return;
      }
      if(keyword)
      {
          for(int i=0; i<21; i++)
          {
              if(Objects.equals(lastToken,keywords[i]))
              {
                  writer.write(lastToken+" (tok_"+keywords[i]+")\n");
                  return;
              }
          }
      }
      
      if(userDef)
      {
          writer.write(lastToken+" (tok_userdef)\n");
          return;
      }
      
      if(Objects.equals(lastToken,"("))
      {
          writer.write(lastToken+" (tok_oparen)\n");
         return; 
      }
      if(Objects.equals(lastToken,")"))
      {
          writer.write(lastToken+" (tok_cparen)\n");
         return; 
      }
      if(Objects.equals(lastToken,"<"))
      {
          writer.write(lastToken+" (tok_lthan)\n");
         return; 
      }
      if(Objects.equals(lastToken,">"))
      {
          writer.write(lastToken+" (tok_gthan)\n");
         return; 
      }
      if(Objects.equals(lastToken,"{"))
      {
          writer.write(lastToken+" (tok_obrace)\n");
         return; 
      }
      if(Objects.equals(lastToken,"}"))
      {
          writer.write(lastToken+" (tok_cbrace)\n");
         return; 
      }
      if(Objects.equals(lastToken,"="))
      {
          writer.write(lastToken+" (tok_equals)\n");
         return; 
      }
      if(Objects.equals(lastToken,","))
      {
          writer.write(lastToken+" (tok_comma)\n");
         return; 
      }
      if(Objects.equals(lastToken,";"))
      {
          writer.write(lastToken+" (tok_semico)\n");
         return; 
      }
      
    }
    
    public static void main(String[] args) {
        try{
       lex();
       parse();
       Scope();
       TypeCheck();
       //valCheck();
       generateBasic();
        }catch(Exception er)
        {
            System.out.println(er.getMessage()+"issue in main");
            
        }
    }
    
}
