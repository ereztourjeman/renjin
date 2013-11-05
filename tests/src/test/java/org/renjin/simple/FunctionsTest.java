package org.renjin.simple;

import org.junit.Test;

public class FunctionsTest extends SimpleTestBase {

  @Test
  public void testDefinitions()  {
    // FIXME: the formatting, white-spaces generated by pretty printer are not exactly like in GNU R
    //TODO: assertEval("x<-function(){1}", "function () { 1.0 }");
    assertEval("{ x<-function(){1} ; x() }", "1.0");
    assertEval("{ x<-function(z){z} ; x(TRUE) }", "TRUE");
    assertEval("{ x<-1 ; f<-function(){x} ; x<-2 ; f() }", "2.0");
    assertEval("{ x<-1 ; f<-function(x){x} ; f(TRUE) }", "TRUE");
    assertEval("{ x<-1 ; f<-function(x){a<-1;b<-2;x} ; f(TRUE) }", "TRUE");
    assertEval("{ f<-function(x){g<-function(x) {x} ; g(x) } ; f(TRUE) }", "TRUE");
    assertEval("{ x<-1 ; f<-function(x){a<-1; b<-2; g<-function(x) {b<-3;x} ; g(b) } ; f(TRUE) }", "2.0");
    assertEval("{ x<-1 ; f<-function(z) { if (z) { x<-2 } ; x } ; x<-3 ; f(FALSE) }", "3.0");
    assertEval("{ f<-function() {z} ; z<-2 ; f() }", "2.0");
    assertEval("{ x<-1 ; g<-function() { x<-12 ; f<-function(z) { if (z) { x<-2 } ; x } ; x<-3 ; f(FALSE) } ; g() }", "3.0");
    assertEval("{ x<-function() { z<-211 ; function(a) { if (a) { z } else { 200 } } } ; f<-x() ; z<-1000 ; f(TRUE) }", "211.0");
    assertEval("{ f<-function(a=1,b=2,c=3) {TRUE} ; f(,,) }", "TRUE");

    assertEval("{ f<-function(x=2) {x} ; f() } ", "2.0");
    assertEval("{ f<-function(a,b,c=2,d) {c} ; f(1,2,c=4,d=4) }", "4.0");
    assertEval("{ f<-function(a,b,c=2,d) {c} ; f(1,2,d=8,c=1) }", "1.0");
    assertEval("{ f<-function(a,b,c=2,d) {c} ; f(1,d=8,2,c=1) }", "1.0");
    assertEval("{ f<-function(a,b,c=2,d) {c} ; f(d=8,1,2,c=1) }", "1.0");
    assertEval("{ f<-function(a,b,c=2,d) {c} ; f(d=8,c=1,2,3) }", "1.0");
    assertEval("{ f<-function(a=10,b,c=20,d=20) {c} ; f(4,3,5,1) }", "5.0");

    assertEval("{ x<-1 ; z<-TRUE ; f<-function(y=x,a=z,b) { if (z) {y} else {z}} ; f(b=2) }", "1.0");
    assertEval("{ x<-1 ; z<-TRUE ; f<-function(y=x,a=z,b) { if (z) {y} else {z}} ; f(2) }", "2.0");
    assertEval("{ x<-1 ; f<-function(x=x) { x } ; f(x=x) }", "1.0");
    assertEval("{ f<-function(z, x=if (z) 2 else 3) {x} ; f(FALSE) }", "3.0");

    assertEval("{f<-function(a,b,c=2,d) {c} ; g <- function() f(d=8,c=1,2,3) ; g() ; g() }", "1.0");

    assertEval("{ f<-function() { return() } ; f() }", "NULL");
    assertEval("{ f<-function() { return(2) ; 3 } ; f() }", "2.0");

    // function matching, builtins
    assertEval("{ x <- function(y) { sum(y) } ; f <- function() { x <- 1 ; x(1:10) } ; f() }", "55L");
    assertEval("{ f <- sum ; f(1:10) }", "55L");
    assertEval("{ x <- function(a,b) { a^b } ; f <- function() { x <- \"sum\" ; sapply(1, x, 2) } ; f() }", "3.0");
    assertEval("{ x <- function(a,b) { a^b } ; g <- function() { x <- \"sum\" ; f <- function() { sapply(1, x, 2) } ; f() }  ; g() }", "3.0");
    assertEval("{ x <- function(a,b) { a^b } ; f <- function() { x <- 211 ; sapply(1, x, 2) } ; f() }", "1.0");
    assertEval("{ x <- function(a,b) { a^b } ; dummy <- sum ; f <- function() { x <- \"dummy\" ; sapply(1, x, 2) } ; f() }", "3.0");
    assertEval("{ x <- function(a,b) { a^b } ; dummy <- sum ; f <- function() { x <- \"dummy\" ; dummy <- 200 ; sapply(1, x, 2) } ; f() }", "3.0");


    // replacement function
    assertEval("{ 'my<-' <- function(x, value) { attr(x, \"myattr\") <- value ; x } ; z <- 1; my(z) <- \"hello\" ; z }", "structure(1, myattr='hello')");
  }

  @Test
  public void delayedAssign() {
    assertEval("{ cnt <- 1 ; delayedAssign(\"z\", evalat <<- cnt ) ; cnt <- 2 ; 'f<-' <- function(x, arg, value) { cnt <<- 4 ; arg * value } ; cnt <- 3; f(z, 12) <- 2 ; evalat }", "3.0");
  }

  @Test
  public void testErrors()  {
    assertEvalError("{ x<-function(){1} ; x(y=1) }", "unused argument(s) (y = 1.0)");
    assertEvalError("{ x<-function(y, b){1} ; x(y=1, 2, 3, z = 5) }", "unused argument(s) (3.0, z = 5.0)");
    assertEvalError("{ x<-function(){1} ; x(1) }", "unused argument(s) (1.0)");
    assertEvalError("{ x<-function(a){1} ; x(1,) }", "unused argument(s) ()");
    assertEvalError("{ x<-function(){1} ; x(y=sum(1:10)) }", "unused argument(s) (y = sum(1.0 : 10.0))");
    assertEvalError("{ f <- function(x) { x } ; f() }", "argument 'x' is missing, with no default");
    assertEvalError("{ x<-function(y,b){1} ; x(y=1,y=3,4) }", "formal argument \"y\" matched by multiple actual arguments");
    assertEvalError("{ x<-function(foo,bar){foo*bar} ; x(fo=10,f=1,2) }", "formal argument \"foo\" matched by multiple actual arguments");

    assertEvalError("{ f <- function(a,a) {1} }", "repeated formal argument 'a'"); // note exactly GNU-R message
    assertEvalError("{ f <- function(a,b,c,d) { a + b } ; f(1,x=1,2,3,4) }", "unused argument(s) (x = 1.0)");
  }

  @Test
  public void testBinding()  {
    assertEval("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(function(x,y) { x + y }, 1, 2) ; myapp(sum, 1, 2) }", "3.0");
    assertEval("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) }", "3.0");
    assertEval("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = c, y = 10, x = 3) }", "3.0, 10.0");
    assertEval("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = function(x,y) { x - y }, y = 10, x = 3) }", "-7.0");
    assertEval("{ myapp <- function(f, x, y) { f(x,y) } ; g <- function(x,y) { x + y } ; myapp(f = g, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = g, y = 10, x = 3) ;  myapp(f = g, y = 11, x = 2) }", "13.0");
    assertEval("{ f <- function(i) { if (i==2) { c <- sum }; c(1,2) } ; f(1) ; f(2) }", "3.0");
    assertEval("{ f <- function(i) { if (i==2) { assign(\"c\", sum) }; c(1,2) } ; f(1) ; f(2) }", "3.0");
    assertEval("{ f <- function(i) { c(1,2) } ; f(1) ; c <- sum ; f(2) }", "3.0");
    assertEval("{ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) }", "4L");
    assertEval("{ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) ; f(length,1:3) }", "3L");
    assertEval("{ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) ; f(function(i) {3}, 1) ; f(length,1:3) }", "3L");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; f(function(x) {TRUE}, 5) ; f(is.na, 4) }", "2.0");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) }", "1.0");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; h <- function(x) { x == x } ; f(h, 3) }", "1.0");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(is.na, 10) }", "2.0");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(c, 10) }", "1.0");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(function(x) { 3+4i }, 10) }", "1.0");
    assertEval("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(is.na, 10) }", "2.0");
    assertEval("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4)  }", "FALSE");
    assertEval("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(is.na, 10) }", "FALSE");
    assertEval("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(length, 10) }", "TRUE");
    assertEval("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 10) ; f(is.na,5) }", "FALSE");
    assertEval("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(function(x) { x + x }, 10) }", "TRUE");
  }

  @Test
  public void testRecursion()  {
    assertEval("{ f<-function(i) { if(i==1) { 1 } else { j<-i-1 ; f(j) } } ; f(10) }", "1.0");
    assertEval("{ f<-function(i) { if(i==1) { 1 } else { f(i-1) } } ; f(10) }", "1.0");
    assertEval("{ f<-function(i) { if(i<=1) 1 else i*f(i-1) } ; f(10) }", "3628800.0"); // factorial
    assertEval("{ f<-function(i) { if(i<=1L) 1L else i*f(i-1L) } ; f(10L) }", "3628800L"); // factorial
    // 100 times calculate factorial of 120
    // the GNU R outputs 6.689503e+198
    assertEval("{ f<-function(i) { if(i<=1) 1 else i*f(i-1) } ; g<-function(n, f, a) { if (n==1) { f(a) } else { f(a) ; g(n-1, f, a) } } ; g(100,f,120) }", "6.689502913449124E198");

    // Fibonacci numbers
    assertEval("{ f<-function(i) { if (i==1) { 1 } else if (i==2) { 1 } else { f(i-1) + f(i-2) } } ; f(10) }", "55.0");
    assertEval("{ f<-function(i) { if (i==1L) { 1L } else if (i==2L) { 1L } else { f(i-1L) + f(i-2L) } } ; f(10L) }", "55L");
  }

  @Test
  public void testPromises()  {
    assertEval("{ f <- function(x = z) { z = 1 ; x } ; f() }", "1.0");
    assertEval("{ z <- 1 ; f <- function(c = z) {  z <- z + 1 ; c  } ; f() }", "2.0");
    assertEval("{ z <- 1 ; f <- function(c = z) { c(1,2) ; z <- z + 1 ; c  } ; f() }", "1.0");
    assertEval("{ f <- function(a) { g <- function(b) { x <<- 2; b } ; g(a) } ; x <- 1 ; f(x) }", "2.0");
    assertEval("{ f <- function(a) { g <- function(b) { a <<- 3; b } ; g(a) } ; x <- 1 ; f(x) }", "3.0");
    assertEval("{ f <- function(x) { function() {x} } ; a <- 1 ; b <- f(a) ; a <- 10 ; b() }", "10.0");
    assertEvalError("{ f <- function(x = y, y = x) { y } ; f() }", "promise already under evaluation: recursive default argument reference?");
  }

  @Test
  public void testMatching()  {
    assertEval("{ x<-function(foo,bar){foo*bar} ; x(f=10,2) }", "20.0");
    assertEval("{ x<-function(foo,bar){foo*bar} ; x(fo=10, bar=2) }", "20.0");
    assertEvalError("{ f <- function(hello, hi) { hello + hi } ; f(h = 1) }", "argument 1 matches multiple formal arguments");
    assertEvalError("{ f <- function(hello, hi) { hello + hi } ; f(hello = 1, bye = 3) }", "unused argument(s) (bye = 3.0)");
    assertEvalError("{ f <- function(a) { a } ; f(1,2) }", "unused argument(s) (2.0)");
  }

  @Test
  public void testDots()  {
    assertEval("{ f <- function(...) { ..1 } ;  f(10) }", "10.0");
    assertEval("{ f <- function(...) { x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "10.0");
    assertEval("{ f <- function(...) { ..1 ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "1.0");
    assertEval("{ f <- function(...) { ..1 ; x <<- 10 ; ..2 } ; x <- 1 ; f(100,x) }", "10.0");
    assertEval("{ f <- function(...) { ..2 ; x <<- 10 ; ..1 } ; x <- 1 ; f(x,100) }", "10.0");
    assertEval("{ g <- function(...) { 0 } ; f <- function(...) { g(...) ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "10.0");
    assertEval("{ f <- function(...) { substitute(..1) } ;  f(x+y) }", "..1");
    assertEval("{ f <- function(...) { g <- function() { ..1 } ; g() } ; f(a=2) }", "2.0");
    assertEval("{ f <- function(...) { ..1 <- 2 ; ..1 } ; f(z = 1) }", "1.0");

    assertEval("{ g <- function(a,b) { a + b } ; f <- function(...) { g(...) }  ; f(1,2) }", "3.0");
    assertEval("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(...,x=4) }  ; f(b=1,a=2) }", "6.0");
    assertEval("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ...) }  ; f(b=1,a=2) }", "6.0");
    assertEval("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ..., 10) }  ; f(b=1) }", "14.0");
    assertEvalError("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ..., 10) }  ; f(b=1,a=2) }", "unused argument(s) (10.0)");
    assertEval("{ g <- function(...) { 0 } ; f <- function(...) { g(...) ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "10.0");
    assertEval("{ g <- function(a,b,aa,bb) { a ; x <<- 10 ; aa ; c(a, aa) } ; f <- function(...) {  g(..., ...) } ; x <- 1; y <- 2; f(x, y) }", "1.0, 1.0");
    assertEval("{ f <- function(a, b) { a - b } ; g <- function(...) { f(1, ...) } ; g(b = 2) }", "-1.0");
    assertEval("{ f <- function(a, b) { a - b } ; g <- function(...) { f(1, ...) } ; g(a = 2) }", "1.0");
    assertEval("{ f <- function(...) { g(...) } ;  g <- function(b=2) { b } ; f() }", "2.0");

    assertEvalError("{ f <- function(...) { ..3 } ; f(1,2) }", "The ... list does not contain 3 elements");
    assertEvalError("{ f <- function() { dummy() } ; f() }", "could not find function 'dummy'"); // note: GNU-R has slightly different error code formatting
    assertEvalError("{ f <- function() { if (FALSE) { dummy <- 2 } ; dummy() } ; f() }", "could not find function 'dummy'");
    assertEvalError("{ f <- function() { if (FALSE) { dummy <- 2 } ; g <- function() { dummy() } ; g() } ; f() }", "could not find function 'dummy'");
    assertEvalError("{ f <- function() { dummy <- 2 ; g <- function() { dummy() } ; g() } ; f() }", "could not find function 'dummy'");
    assertEvalError("{ f <- function() { dummy() } ; dummy <- 2 ; f() }", "could not find function 'dummy'");
    assertEvalError("{ dummy <- 2 ; dummy() }", "could not find function 'dummy'");
    assertEvalError("{ lapply(1:3, \"dummy\") }", "object 'dummy' of mode 'function' was not found");

    assertEvalError("{ f <- function(a, b) { a + b } ; g <- function(...) { f(a=1, ...) } ; g(a=2) }", "formal argument \"a\" matched by multiple actual arguments");
    assertEval("{ f <- function(a, barg) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2) }", "3.0");
    assertEval("{ f <- function(a, barg, ...) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2,3) }", "3.0");
    assertEvalError("{ f <- function(a, barg, bextra) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2,3) }", "argument 2 matches multiple formal arguments");
    assertEval("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(be=2,du=3, 3) }", "4.0");
    assertEvalError("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(be=2,bex=3, 3) }", "formal argument \"bextra\" matched by multiple actual arguments");
    assertEvalError("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ..., x=2) } ; g(1) }", "unused argument(s) (x = 2.0)");
    assertEvalError("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ..., x=2,z=3) } ; g(1) }", "unused argument(s) (x = 2.0, z = 3.0)");
    assertEvalError("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ..., xxx=2) } ; g(1) }", "unused argument(s) (xxx = 2.0)");
    assertEvalError("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, xxx=2, ...) } ; g(1) }", "unused argument(s) (xxx = 2.0)");
    assertEval("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(1,2,3) }", "2.0");
    assertEval("{ f <- function(a, b) { a * b } ; g <- function(...) { f(...,...) } ; g(3) }", "9.0");
    assertEval("{ g <- function(...) { c(...,...) } ; g(3) }", "3.0, 3.0");
    assertEvalError("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...,,,) } ; g(1) }", "unused argument(s) ()");
    assertEval("{ f <- function(...,d) { ..1 + ..2 } ; f(1,d=4,2) }", "3.0");
    assertEval("{ f <- function(...,d) { ..1 + ..2 } ; f(1,2,d=4) }", "3.0");

    assertEvalError("{ f <- function(...) { ..2 + ..2 } ; f(1,,2) }", "'..2' is missing");
    assertEvalError("{ f <- function(...) { ..1 + ..2 } ; f(1,,3) }", "'..2' is missing");
  }
}
