/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 211 "/usr/lib/gcc/x86_64-linux-gnu/4.4.3/include/stddef.h"
typedef unsigned long size_t;
#line 471 "/usr/include/stdlib.h"
extern  __attribute__((__nothrow__)) void *malloc(size_t __size )  __attribute__((__malloc__)) ;
#line 71 "/usr/include/assert.h"
extern  __attribute__((__nothrow__, __noreturn__)) void __assert_fail(char const   *__assertion ,
                                                                      char const   *__file ,
                                                                      unsigned int __line ,
                                                                      char const   *__function ) ;
#line 76 "my-util.c"
void *xalloc(size_t sz ) 
{ void *p ;
  void *tmp ;
  void *__cil_tmp4 ;
  unsigned long __cil_tmp5 ;
  unsigned long __cil_tmp6 ;

  {
#line 80
  if (sz > 0UL) {

  } else {
    {
#line 80
    __assert_fail("sz>0", "my-util.c", 80U, "xalloc");
    }
  }
  {
#line 82
  tmp = malloc(sz);
#line 82
  p = tmp;
  }
  {
#line 83
  __cil_tmp4 = (void *)0;
#line 83
  __cil_tmp5 = (unsigned long )__cil_tmp4;
#line 83
  __cil_tmp6 = (unsigned long )p;
#line 83
  if (__cil_tmp6 != __cil_tmp5) {

  } else {
    {
#line 83
    __assert_fail("p!=((void *)0)", "my-util.c", 83U, "xalloc");
    }
  }
  }
#line 85
  return (p);
}
}
#line 95 "my-util.c"
size_t strlcpy(char *dst , char const   *src , size_t siz ) 
{ register char *d ;
  register char const   *s ;
  register size_t n ;
  char *tmp ;
  char tmp___0 ;
  char const   *tmp___1 ;
  char const   *tmp___2 ;
  char __cil_tmp11 ;
  int __cil_tmp12 ;
  int __cil_tmp13 ;
  int __cil_tmp14 ;

  {
#line 98
  d = dst;
#line 99
  s = src;
#line 100
  n = siz;
#line 103
  if (n != 0UL) {
#line 103
    n = n - 1UL;
#line 103
    if (n != 0UL) {
      {
#line 104
      while (1) {
        while_continue: /* CIL Label */ ;
#line 105
        tmp = d;
#line 105
        d = d + 1;
#line 105
        tmp___1 = s;
#line 105
        s = s + 1;
#line 105
        __cil_tmp11 = *tmp___1;
#line 105
        tmp___0 = (char )__cil_tmp11;
#line 105
        *tmp = tmp___0;
        {
#line 105
        __cil_tmp12 = (int )tmp___0;
#line 105
        if (__cil_tmp12 == 0) {
#line 106
          goto while_break;
        } else {

        }
        }
#line 104
        n = n - 1UL;
#line 104
        if (n != 0UL) {

        } else {
#line 104
          goto while_break;
        }
      }
      while_break: /* CIL Label */ ;
      }
    } else {

    }
  } else {

  }
#line 111
  if (n == 0UL) {
#line 112
    if (siz != 0UL) {
#line 113
      *d = (char )'\000';
    } else {

    }
    {
#line 114
    while (1) {
      while_continue___0: /* CIL Label */ ;
#line 114
      tmp___2 = s;
#line 114
      s = s + 1;
#line 114
      if (*tmp___2) {

      } else {
#line 114
        goto while_break___0;
      }
    }
    while_break___0: /* CIL Label */ ;
    }
  } else {

  }
  {
#line 118
  __cil_tmp13 = s - src;
#line 118
  __cil_tmp14 = __cil_tmp13 - 1;
#line 118
  return ((size_t )__cil_tmp14);
  }
}
}
