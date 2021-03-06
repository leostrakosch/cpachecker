/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 214 "/usr/lib/gcc/i486-linux-gnu/4.3.3/include/stddef.h"
typedef unsigned int size_t;
#line 56 "/usr/include/bits/types.h"
typedef long long __quad_t;
#line 141 "/usr/include/bits/types.h"
typedef long __off_t;
#line 142 "/usr/include/bits/types.h"
typedef __quad_t __off64_t;
#line 45 "/usr/include/stdio.h"
struct _IO_FILE;
#line 45
struct _IO_FILE;
#line 49 "/usr/include/stdio.h"
typedef struct _IO_FILE FILE;
#line 170 "/usr/include/libio.h"
struct _IO_FILE;
#line 180 "/usr/include/libio.h"
typedef void _IO_lock_t;
#line 186 "/usr/include/libio.h"
struct _IO_marker {
   struct _IO_marker *_next ;
   struct _IO_FILE *_sbuf ;
   int _pos ;
};
#line 271 "/usr/include/libio.h"
struct _IO_FILE {
   int _flags ;
   char *_IO_read_ptr ;
   char *_IO_read_end ;
   char *_IO_read_base ;
   char *_IO_write_base ;
   char *_IO_write_ptr ;
   char *_IO_write_end ;
   char *_IO_buf_base ;
   char *_IO_buf_end ;
   char *_IO_save_base ;
   char *_IO_backup_base ;
   char *_IO_save_end ;
   struct _IO_marker *_markers ;
   struct _IO_FILE *_chain ;
   int _fileno ;
   int _flags2 ;
   __off_t _old_offset ;
   unsigned short _cur_column ;
   signed char _vtable_offset ;
   char _shortbuf[1] ;
   _IO_lock_t *_lock ;
   __off64_t _offset ;
   void *__pad1 ;
   void *__pad2 ;
   void *__pad3 ;
   void *__pad4 ;
   size_t __pad5 ;
   int _mode ;
   char _unused2[(15U * sizeof(int ) - 4U * sizeof(void *)) - sizeof(size_t )] ;
};
#line 21 "em3d.h"
struct node_t {
   double value ;
   struct node_t *next ;
   struct node_t **to_nodes ;
   struct node_t **from_nodes ;
   double *coeffs ;
   int from_count ;
};
#line 21 "em3d.h"
typedef struct node_t node_t;
#line 30 "em3d.h"
struct graph_t {
   node_t *e_nodes ;
   node_t *h_nodes ;
};
#line 30 "em3d.h"
typedef struct graph_t graph_t;
#line 147 "/usr/include/stdio.h"
extern struct _IO_FILE *stderr ;
#line 331
extern int fprintf(FILE * __restrict  __stream , char const   * __restrict  __format 
                   , ...) ;
#line 337
extern int printf(char const   * __restrict  __format  , ...) ;
#line 11 "em3d.h"
int n_nodes  ;
#line 12 "em3d.h"
int d_nodes  ;
#line 13 "em3d.h"
int iters  ;
#line 36
void compute_nodes(node_t *nodelist ) ;
#line 13 "make_graph.h"
graph_t initialize_graph(void) ;
#line 148 "/usr/include/stdlib.h"
extern  __attribute__((__nothrow__)) int atoi(char const   *__nptr )  __attribute__((__pure__,
__nonnull__(1))) ;
#line 395
extern  __attribute__((__nothrow__)) double drand48(void) ;
#line 399
extern  __attribute__((__nothrow__)) long lrand48(void) ;
#line 409
extern  __attribute__((__nothrow__)) void srand48(long __seedval ) ;
#line 471
extern  __attribute__((__nothrow__)) void *malloc(size_t __size )  __attribute__((__malloc__)) ;
#line 488
extern  __attribute__((__nothrow__)) void free(void *__ptr ) ;
#line 531
extern  __attribute__((__nothrow__, __noreturn__)) void exit(int __status ) ;
#line 5 "util.c"
void init_random(int myid ) 
{ int __cil_tmp2 ;
  long __cil_tmp3 ;

  {
  {
#line 7
  __cil_tmp2 = myid * 45;
#line 7
  __cil_tmp3 = (long )__cil_tmp2;
#line 7
  srand48(__cil_tmp3);
  }
#line 8
  return;
}
}
#line 11 "util.c"
int gen_number(int range ) 
{ long tmp ;
  long __cil_tmp3 ;
  long __cil_tmp4 ;

  {
  {
#line 13
  tmp = lrand48();
  }
  {
#line 13
  __cil_tmp3 = (long )range;
#line 13
  __cil_tmp4 = tmp % __cil_tmp3;
#line 13
  return ((int )__cil_tmp4);
  }
}
}
#line 17 "util.c"
int gen_signed_number(int range ) 
{ int temp ;
  long tmp ;
  int __cil_tmp4 ;
  int __cil_tmp5 ;
  long __cil_tmp6 ;
  long __cil_tmp7 ;
  int __cil_tmp8 ;

  {
  {
#line 21
  tmp = lrand48();
#line 21
  __cil_tmp4 = 2 * range;
#line 21
  __cil_tmp5 = __cil_tmp4 - 1;
#line 21
  __cil_tmp6 = (long )__cil_tmp5;
#line 21
  __cil_tmp7 = tmp % __cil_tmp6;
#line 21
  temp = (int )__cil_tmp7;
  }
  {
#line 22
  __cil_tmp8 = range - 1;
#line 22
  return (temp - __cil_tmp8);
  }
}
}
#line 26 "util.c"
double gen_uniform_double(void) 
{ double tmp ;

  {
  {
#line 28
  tmp = drand48();
  }
#line 28
  return (tmp);
}
}
#line 31 "util.c"
int check_percent(int percent ) 
{ double tmp ;
  double __cil_tmp3 ;
  double __cil_tmp4 ;

  {
  {
#line 33
  tmp = drand48();
  }
  {
#line 33
  __cil_tmp3 = (double )percent;
#line 33
  __cil_tmp4 = __cil_tmp3 / 100.0;
#line 33
  return (tmp < __cil_tmp4);
  }
}
}
#line 3 "em3d.c"
void compute_nodes(node_t *nodelist ) 
{ int i ;
  node_t *other_node ;
  double coeff ;
  double value ;
  unsigned int __cil_tmp6 ;
  unsigned int __cil_tmp7 ;
  int __cil_tmp8 ;
  unsigned int __cil_tmp9 ;
  unsigned int __cil_tmp10 ;
  struct node_t **__cil_tmp11 ;
  struct node_t **__cil_tmp12 ;
  unsigned int __cil_tmp13 ;
  unsigned int __cil_tmp14 ;
  double *__cil_tmp15 ;
  double *__cil_tmp16 ;
  double __cil_tmp17 ;
  double __cil_tmp18 ;
  unsigned int __cil_tmp19 ;
  unsigned int __cil_tmp20 ;
  int *mem_21 ;
  struct node_t ***mem_22 ;
  double **mem_23 ;
  double *mem_24 ;
  double *mem_25 ;
  double *mem_26 ;
  struct node_t **mem_27 ;

  {
  {
#line 7
  while (1) {
    while_0_continue: /* CIL Label */ ;
#line 7
    if (nodelist) {

    } else {
      goto while_0_break;
    }
#line 8
    i = 0;
    {
#line 8
    while (1) {
      while_1_continue: /* CIL Label */ ;
      {
#line 8
      __cil_tmp6 = (unsigned int )nodelist;
#line 8
      __cil_tmp7 = __cil_tmp6 + 24;
#line 8
      mem_21 = (int *)__cil_tmp7;
#line 8
      __cil_tmp8 = *mem_21;
#line 8
      if (i < __cil_tmp8) {

      } else {
        goto while_1_break;
      }
      }
#line 10
      __cil_tmp9 = (unsigned int )nodelist;
#line 10
      __cil_tmp10 = __cil_tmp9 + 16;
#line 10
      mem_22 = (struct node_t ***)__cil_tmp10;
#line 10
      __cil_tmp11 = *mem_22;
#line 10
      __cil_tmp12 = __cil_tmp11 + i;
#line 10
      other_node = *__cil_tmp12;
#line 11
      __cil_tmp13 = (unsigned int )nodelist;
#line 11
      __cil_tmp14 = __cil_tmp13 + 20;
#line 11
      mem_23 = (double **)__cil_tmp14;
#line 11
      __cil_tmp15 = *mem_23;
#line 11
      __cil_tmp16 = __cil_tmp15 + i;
#line 11
      coeff = *__cil_tmp16;
#line 12
      mem_24 = (double *)other_node;
#line 12
      value = *mem_24;
#line 14
      __cil_tmp17 = coeff * value;
#line 14
      mem_25 = (double *)nodelist;
#line 14
      __cil_tmp18 = *mem_25;
#line 14
      mem_26 = (double *)nodelist;
#line 14
      *mem_26 = __cil_tmp18 - __cil_tmp17;
#line 8
      i = i + 1;
    }
    while_1_break: /* CIL Label */ ;
    }
#line 7
    __cil_tmp19 = (unsigned int )nodelist;
#line 7
    __cil_tmp20 = __cil_tmp19 + 8;
#line 7
    mem_27 = (struct node_t **)__cil_tmp20;
#line 7
    nodelist = *mem_27;
  }
  while_0_break: /* CIL Label */ ;
  }
#line 16
  return;
}
}
#line 23 "make_graph.c"
node_t **make_table(int size ) 
{ node_t **retval ;
  void *tmp ;
  unsigned int __cil_tmp4 ;
  unsigned int __cil_tmp5 ;
  char const   * __restrict  __cil_tmp6 ;

  {
  {
#line 27
  __cil_tmp4 = (unsigned int )size;
#line 27
  __cil_tmp5 = __cil_tmp4 * 4U;
#line 27
  tmp = malloc(__cil_tmp5);
#line 27
  retval = (node_t **)tmp;
  }
#line 28
  if (! retval) {
    {
#line 28
    __cil_tmp6 = (char const   * __restrict  )"Assertion failure\n";
#line 28
    printf(__cil_tmp6);
#line 28
    exit(-1);
    }
  } else {

  }
#line 29
  return (retval);
}
}
#line 35 "make_graph.c"
void fill_table(node_t **table , int size ) 
{ int i ;
  void *tmp ;
  node_t **__cil_tmp5 ;
  node_t **__cil_tmp6 ;
  node_t *__cil_tmp7 ;
  node_t **__cil_tmp8 ;
  node_t *__cil_tmp9 ;
  unsigned int __cil_tmp10 ;
  unsigned int __cil_tmp11 ;
  int __cil_tmp12 ;
  node_t **__cil_tmp13 ;
  node_t *__cil_tmp14 ;
  unsigned int __cil_tmp15 ;
  unsigned int __cil_tmp16 ;
  node_t **__cil_tmp17 ;
  int __cil_tmp18 ;
  node_t **__cil_tmp19 ;
  node_t *__cil_tmp20 ;
  unsigned int __cil_tmp21 ;
  unsigned int __cil_tmp22 ;
  void *__cil_tmp23 ;
  double *mem_24 ;
  int *mem_25 ;
  struct node_t **mem_26 ;
  struct node_t **mem_27 ;

  {
#line 40
  i = 0;
  {
#line 40
  while (1) {
    while_2_continue: /* CIL Label */ ;
#line 40
    if (i < size) {

    } else {
      goto while_2_break;
    }
    {
#line 42
    tmp = malloc(28U);
#line 42
    __cil_tmp5 = table + i;
#line 42
    *__cil_tmp5 = (node_t *)tmp;
#line 43
    __cil_tmp6 = table + i;
#line 43
    __cil_tmp7 = *__cil_tmp6;
#line 43
    mem_24 = (double *)__cil_tmp7;
#line 43
    *mem_24 = gen_uniform_double();
#line 44
    __cil_tmp8 = table + i;
#line 44
    __cil_tmp9 = *__cil_tmp8;
#line 44
    __cil_tmp10 = (unsigned int )__cil_tmp9;
#line 44
    __cil_tmp11 = __cil_tmp10 + 24;
#line 44
    mem_25 = (int *)__cil_tmp11;
#line 44
    *mem_25 = 0;
    }
#line 46
    if (i > 0) {
#line 47
      __cil_tmp12 = i - 1;
#line 47
      __cil_tmp13 = table + __cil_tmp12;
#line 47
      __cil_tmp14 = *__cil_tmp13;
#line 47
      __cil_tmp15 = (unsigned int )__cil_tmp14;
#line 47
      __cil_tmp16 = __cil_tmp15 + 8;
#line 47
      __cil_tmp17 = table + i;
#line 47
      mem_26 = (struct node_t **)__cil_tmp16;
#line 47
      *mem_26 = *__cil_tmp17;
    } else {

    }
#line 40
    i = i + 1;
  }
  while_2_break: /* CIL Label */ ;
  }
#line 49
  __cil_tmp18 = size - 1;
#line 49
  __cil_tmp19 = table + __cil_tmp18;
#line 49
  __cil_tmp20 = *__cil_tmp19;
#line 49
  __cil_tmp21 = (unsigned int )__cil_tmp20;
#line 49
  __cil_tmp22 = __cil_tmp21 + 8;
#line 49
  __cil_tmp23 = (void *)0;
#line 49
  mem_27 = (struct node_t **)__cil_tmp22;
#line 49
  *mem_27 = (struct node_t *)__cil_tmp23;
#line 50
  return;
}
}
#line 57 "make_graph.c"
void make_neighbors(node_t *nodelist , node_t **table , int tablesz , int degree ) 
{ node_t *cur_node ;
  node_t *other_node ;
  int j ;
  int k ;
  void *tmp ;
  int tmp___0 ;
  unsigned int __cil_tmp11 ;
  unsigned int __cil_tmp12 ;
  unsigned int __cil_tmp13 ;
  unsigned int __cil_tmp14 ;
  node_t **__cil_tmp15 ;
  unsigned int __cil_tmp16 ;
  unsigned int __cil_tmp17 ;
  struct node_t **__cil_tmp18 ;
  struct node_t **__cil_tmp19 ;
  struct node_t *__cil_tmp20 ;
  unsigned int __cil_tmp21 ;
  unsigned int __cil_tmp22 ;
  unsigned int __cil_tmp23 ;
  unsigned int __cil_tmp24 ;
  struct node_t **__cil_tmp25 ;
  struct node_t **__cil_tmp26 ;
  unsigned int __cil_tmp27 ;
  unsigned int __cil_tmp28 ;
  unsigned int __cil_tmp29 ;
  unsigned int __cil_tmp30 ;
  int __cil_tmp31 ;
  unsigned int __cil_tmp32 ;
  unsigned int __cil_tmp33 ;
  struct node_t ***mem_34 ;
  struct node_t ***mem_35 ;
  struct node_t ***mem_36 ;
  int *mem_37 ;
  int *mem_38 ;
  struct node_t **mem_39 ;

  {
#line 62
  cur_node = nodelist;
  {
#line 62
  while (1) {
    while_3_continue: /* CIL Label */ ;
#line 62
    if (cur_node) {

    } else {
      goto while_3_break;
    }
    {
#line 67
    __cil_tmp11 = (unsigned int )degree;
#line 67
    __cil_tmp12 = __cil_tmp11 * 4U;
#line 67
    tmp = malloc(__cil_tmp12);
#line 67
    __cil_tmp13 = (unsigned int )cur_node;
#line 67
    __cil_tmp14 = __cil_tmp13 + 12;
#line 67
    mem_34 = (struct node_t ***)__cil_tmp14;
#line 67
    *mem_34 = (node_t **)tmp;
#line 68
    j = 0;
    }
    {
#line 68
    while (1) {
      while_4_continue: /* CIL Label */ ;
#line 68
      if (j < degree) {

      } else {
        goto while_4_break;
      }
      {
#line 71
      while (1) {
        while_5_continue: /* CIL Label */ ;
        {
#line 73
        tmp___0 = gen_number(tablesz);
#line 73
        __cil_tmp15 = table + tmp___0;
#line 73
        other_node = *__cil_tmp15;
#line 74
        k = 0;
        }
        {
#line 74
        while (1) {
          while_6_continue: /* CIL Label */ ;
#line 74
          if (k < j) {

          } else {
            goto while_6_break;
          }
          {
#line 75
          __cil_tmp16 = (unsigned int )cur_node;
#line 75
          __cil_tmp17 = __cil_tmp16 + 12;
#line 75
          mem_35 = (struct node_t ***)__cil_tmp17;
#line 75
          __cil_tmp18 = *mem_35;
#line 75
          __cil_tmp19 = __cil_tmp18 + k;
#line 75
          __cil_tmp20 = *__cil_tmp19;
#line 75
          __cil_tmp21 = (unsigned int )__cil_tmp20;
#line 75
          __cil_tmp22 = (unsigned int )other_node;
#line 75
          if (__cil_tmp22 == __cil_tmp21) {
            goto while_6_break;
          } else {

          }
          }
#line 74
          k = k + 1;
        }
        while_6_break: /* CIL Label */ ;
        }
#line 71
        if (k < j) {

        } else {
          goto while_5_break;
        }
      }
      while_5_break: /* CIL Label */ ;
      }
#line 78
      __cil_tmp23 = (unsigned int )cur_node;
#line 78
      __cil_tmp24 = __cil_tmp23 + 12;
#line 78
      mem_36 = (struct node_t ***)__cil_tmp24;
#line 78
      __cil_tmp25 = *mem_36;
#line 78
      __cil_tmp26 = __cil_tmp25 + j;
#line 78
      *__cil_tmp26 = other_node;
#line 79
      __cil_tmp27 = (unsigned int )other_node;
#line 79
      __cil_tmp28 = __cil_tmp27 + 24;
#line 79
      __cil_tmp29 = (unsigned int )other_node;
#line 79
      __cil_tmp30 = __cil_tmp29 + 24;
#line 79
      mem_37 = (int *)__cil_tmp30;
#line 79
      __cil_tmp31 = *mem_37;
#line 79
      mem_38 = (int *)__cil_tmp28;
#line 79
      *mem_38 = __cil_tmp31 + 1;
#line 68
      j = j + 1;
    }
    while_4_break: /* CIL Label */ ;
    }
#line 62
    __cil_tmp32 = (unsigned int )cur_node;
#line 62
    __cil_tmp33 = __cil_tmp32 + 8;
#line 62
    mem_39 = (struct node_t **)__cil_tmp33;
#line 62
    cur_node = *mem_39;
  }
  while_3_break: /* CIL Label */ ;
  }
#line 82
  return;
}
}
#line 88 "make_graph.c"
void update_from_coeffs(node_t *nodelist ) 
{ node_t *cur_node ;
  int from_count ;
  int k ;
  void *tmp ;
  void *tmp___0 ;
  unsigned int __cil_tmp7 ;
  unsigned int __cil_tmp8 ;
  unsigned int __cil_tmp9 ;
  unsigned int __cil_tmp10 ;
  unsigned int __cil_tmp11 ;
  unsigned int __cil_tmp12 ;
  unsigned int __cil_tmp13 ;
  unsigned int __cil_tmp14 ;
  unsigned int __cil_tmp15 ;
  unsigned int __cil_tmp16 ;
  unsigned int __cil_tmp17 ;
  unsigned int __cil_tmp18 ;
  double *__cil_tmp19 ;
  double *__cil_tmp20 ;
  unsigned int __cil_tmp21 ;
  unsigned int __cil_tmp22 ;
  unsigned int __cil_tmp23 ;
  unsigned int __cil_tmp24 ;
  int *mem_25 ;
  struct node_t ***mem_26 ;
  double **mem_27 ;
  double **mem_28 ;
  int *mem_29 ;
  struct node_t **mem_30 ;

  {
#line 93
  cur_node = nodelist;
  {
#line 93
  while (1) {
    while_7_continue: /* CIL Label */ ;
#line 93
    if (cur_node) {

    } else {
      goto while_7_break;
    }
    {
#line 95
    __cil_tmp7 = (unsigned int )cur_node;
#line 95
    __cil_tmp8 = __cil_tmp7 + 24;
#line 95
    mem_25 = (int *)__cil_tmp8;
#line 95
    from_count = *mem_25;
#line 98
    __cil_tmp9 = (unsigned int )from_count;
#line 98
    __cil_tmp10 = __cil_tmp9 * 4U;
#line 98
    tmp = malloc(__cil_tmp10);
#line 98
    __cil_tmp11 = (unsigned int )cur_node;
#line 98
    __cil_tmp12 = __cil_tmp11 + 16;
#line 98
    mem_26 = (struct node_t ***)__cil_tmp12;
#line 98
    *mem_26 = (node_t **)tmp;
#line 99
    __cil_tmp13 = (unsigned int )from_count;
#line 99
    __cil_tmp14 = __cil_tmp13 * 8U;
#line 99
    tmp___0 = malloc(__cil_tmp14);
#line 99
    __cil_tmp15 = (unsigned int )cur_node;
#line 99
    __cil_tmp16 = __cil_tmp15 + 20;
#line 99
    mem_27 = (double **)__cil_tmp16;
#line 99
    *mem_27 = (double *)tmp___0;
#line 100
    k = 0;
    }
    {
#line 100
    while (1) {
      while_8_continue: /* CIL Label */ ;
#line 100
      if (k < from_count) {

      } else {
        goto while_8_break;
      }
      {
#line 101
      __cil_tmp17 = (unsigned int )cur_node;
#line 101
      __cil_tmp18 = __cil_tmp17 + 20;
#line 101
      mem_28 = (double **)__cil_tmp18;
#line 101
      __cil_tmp19 = *mem_28;
#line 101
      __cil_tmp20 = __cil_tmp19 + k;
#line 101
      *__cil_tmp20 = gen_uniform_double();
#line 100
      k = k + 1;
      }
    }
    while_8_break: /* CIL Label */ ;
    }
#line 103
    __cil_tmp21 = (unsigned int )cur_node;
#line 103
    __cil_tmp22 = __cil_tmp21 + 24;
#line 103
    mem_29 = (int *)__cil_tmp22;
#line 103
    *mem_29 = 0;
#line 93
    __cil_tmp23 = (unsigned int )cur_node;
#line 93
    __cil_tmp24 = __cil_tmp23 + 8;
#line 93
    mem_30 = (struct node_t **)__cil_tmp24;
#line 93
    cur_node = *mem_30;
  }
  while_7_break: /* CIL Label */ ;
  }
#line 105
  return;
}
}
#line 114 "make_graph.c"
void fill_from_fields(node_t *nodelist , int degree ) 
{ node_t *cur_node ;
  int j ;
  node_t *other_node ;
  unsigned int __cil_tmp6 ;
  unsigned int __cil_tmp7 ;
  struct node_t **__cil_tmp8 ;
  struct node_t **__cil_tmp9 ;
  unsigned int __cil_tmp10 ;
  unsigned int __cil_tmp11 ;
  int __cil_tmp12 ;
  unsigned int __cil_tmp13 ;
  unsigned int __cil_tmp14 ;
  struct node_t **__cil_tmp15 ;
  struct node_t **__cil_tmp16 ;
  unsigned int __cil_tmp17 ;
  unsigned int __cil_tmp18 ;
  unsigned int __cil_tmp19 ;
  unsigned int __cil_tmp20 ;
  int __cil_tmp21 ;
  unsigned int __cil_tmp22 ;
  unsigned int __cil_tmp23 ;
  struct node_t ***mem_24 ;
  int *mem_25 ;
  struct node_t ***mem_26 ;
  int *mem_27 ;
  int *mem_28 ;
  struct node_t **mem_29 ;

  {
#line 117
  cur_node = nodelist;
  {
#line 117
  while (1) {
    while_9_continue: /* CIL Label */ ;
#line 117
    if (cur_node) {

    } else {
      goto while_9_break;
    }
#line 121
    j = 0;
    {
#line 121
    while (1) {
      while_10_continue: /* CIL Label */ ;
#line 121
      if (j < degree) {

      } else {
        goto while_10_break;
      }
#line 123
      __cil_tmp6 = (unsigned int )cur_node;
#line 123
      __cil_tmp7 = __cil_tmp6 + 12;
#line 123
      mem_24 = (struct node_t ***)__cil_tmp7;
#line 123
      __cil_tmp8 = *mem_24;
#line 123
      __cil_tmp9 = __cil_tmp8 + j;
#line 123
      other_node = *__cil_tmp9;
#line 124
      __cil_tmp10 = (unsigned int )other_node;
#line 124
      __cil_tmp11 = __cil_tmp10 + 24;
#line 124
      mem_25 = (int *)__cil_tmp11;
#line 124
      __cil_tmp12 = *mem_25;
#line 124
      __cil_tmp13 = (unsigned int )other_node;
#line 124
      __cil_tmp14 = __cil_tmp13 + 16;
#line 124
      mem_26 = (struct node_t ***)__cil_tmp14;
#line 124
      __cil_tmp15 = *mem_26;
#line 124
      __cil_tmp16 = __cil_tmp15 + __cil_tmp12;
#line 124
      *__cil_tmp16 = cur_node;
#line 125
      __cil_tmp17 = (unsigned int )other_node;
#line 125
      __cil_tmp18 = __cil_tmp17 + 24;
#line 125
      __cil_tmp19 = (unsigned int )other_node;
#line 125
      __cil_tmp20 = __cil_tmp19 + 24;
#line 125
      mem_27 = (int *)__cil_tmp20;
#line 125
      __cil_tmp21 = *mem_27;
#line 125
      mem_28 = (int *)__cil_tmp18;
#line 125
      *mem_28 = __cil_tmp21 + 1;
#line 121
      j = j + 1;
    }
    while_10_break: /* CIL Label */ ;
    }
#line 117
    __cil_tmp22 = (unsigned int )cur_node;
#line 117
    __cil_tmp23 = __cil_tmp22 + 8;
#line 117
    mem_29 = (struct node_t **)__cil_tmp23;
#line 117
    cur_node = *mem_29;
  }
  while_9_break: /* CIL Label */ ;
  }
#line 128
  return;
}
}
#line 136 "make_graph.c"
graph_t initialize_graph(void) 
{ node_t **h_table ;
  node_t **e_table ;
  graph_t retval ;
  node_t **__cil_tmp4 ;
  node_t *__cil_tmp5 ;
  node_t **__cil_tmp6 ;
  node_t *__cil_tmp7 ;
  node_t **__cil_tmp8 ;
  node_t *__cil_tmp9 ;
  node_t **__cil_tmp10 ;
  node_t *__cil_tmp11 ;
  node_t **__cil_tmp12 ;
  node_t *__cil_tmp13 ;
  node_t **__cil_tmp14 ;
  node_t *__cil_tmp15 ;
  node_t **__cil_tmp16 ;
  node_t **__cil_tmp17 ;
  void *__cil_tmp18 ;
  void *__cil_tmp19 ;

  {
  {
#line 143
  h_table = make_table(n_nodes);
#line 144
  fill_table(h_table, n_nodes);
#line 147
  e_table = make_table(n_nodes);
#line 148
  fill_table(e_table, n_nodes);
#line 152
  __cil_tmp4 = h_table + 0;
#line 152
  __cil_tmp5 = *__cil_tmp4;
#line 152
  make_neighbors(__cil_tmp5, e_table, n_nodes, d_nodes);
#line 153
  __cil_tmp6 = e_table + 0;
#line 153
  __cil_tmp7 = *__cil_tmp6;
#line 153
  make_neighbors(__cil_tmp7, h_table, n_nodes, d_nodes);
#line 156
  __cil_tmp8 = h_table + 0;
#line 156
  __cil_tmp9 = *__cil_tmp8;
#line 156
  update_from_coeffs(__cil_tmp9);
#line 157
  __cil_tmp10 = e_table + 0;
#line 157
  __cil_tmp11 = *__cil_tmp10;
#line 157
  update_from_coeffs(__cil_tmp11);
#line 160
  __cil_tmp12 = h_table + 0;
#line 160
  __cil_tmp13 = *__cil_tmp12;
#line 160
  fill_from_fields(__cil_tmp13, d_nodes);
#line 161
  __cil_tmp14 = e_table + 0;
#line 161
  __cil_tmp15 = *__cil_tmp14;
#line 161
  fill_from_fields(__cil_tmp15, d_nodes);
#line 163
  __cil_tmp16 = e_table + 0;
#line 163
  retval.e_nodes = *__cil_tmp16;
#line 164
  __cil_tmp17 = h_table + 0;
#line 164
  retval.h_nodes = *__cil_tmp17;
#line 166
  __cil_tmp18 = (void *)h_table;
#line 166
  free(__cil_tmp18);
#line 167
  __cil_tmp19 = (void *)e_table;
#line 167
  free(__cil_tmp19);
  }
#line 169
  return (retval);
}
}
#line 5 "args.c"
void dealwithargs(int argc , char **argv ) 
{ char **__cil_tmp3 ;
  char *__cil_tmp4 ;
  char const   *__cil_tmp5 ;
  char **__cil_tmp6 ;
  char *__cil_tmp7 ;
  char const   *__cil_tmp8 ;
  char **__cil_tmp9 ;
  char *__cil_tmp10 ;
  char const   *__cil_tmp11 ;

  {
#line 7
  if (argc > 1) {
    {
#line 8
    __cil_tmp3 = argv + 1;
#line 8
    __cil_tmp4 = *__cil_tmp3;
#line 8
    __cil_tmp5 = (char const   *)__cil_tmp4;
#line 8
    n_nodes = atoi(__cil_tmp5);
    }
  } else {
#line 10
    n_nodes = 10;
  }
#line 12
  if (argc > 2) {
    {
#line 13
    __cil_tmp6 = argv + 2;
#line 13
    __cil_tmp7 = *__cil_tmp6;
#line 13
    __cil_tmp8 = (char const   *)__cil_tmp7;
#line 13
    d_nodes = atoi(__cil_tmp8);
    }
  } else {
#line 15
    d_nodes = 3;
  }
#line 17
  if (argc > 3) {
    {
#line 18
    __cil_tmp9 = argv + 3;
#line 18
    __cil_tmp10 = *__cil_tmp9;
#line 18
    __cil_tmp11 = (char const   *)__cil_tmp10;
#line 18
    iters = atoi(__cil_tmp11);
    }
  } else {
#line 20
    iters = 100;
  }
#line 21
  return;
}
}
#line 12 "main.c"
void print_graph(node_t *graph_e_nodes3 , node_t *graph_h_nodes2 ) 
{ 

  {
#line 28
  return;
}
}
#line 32 "main.c"
int main(int argc , char **argv ) 
{ int i ;
  graph_t graph ;
  FILE * __restrict  __cil_tmp5 ;
  char const   * __restrict  __cil_tmp6 ;

  {
  {
#line 37
  dealwithargs(argc, argv);
#line 38
  graph = initialize_graph();
#line 39
  print_graph(graph.e_nodes, graph.h_nodes);
#line 41
  i = 0;
  }
  {
#line 41
  while (1) {
    while_11_continue: /* CIL Label */ ;
#line 41
    if (i < iters) {

    } else {
      goto while_11_break;
    }
    {
#line 43
    compute_nodes(graph.e_nodes);
#line 44
    compute_nodes(graph.h_nodes);
#line 45
    __cil_tmp5 = (FILE * __restrict  )stderr;
#line 45
    __cil_tmp6 = (char const   * __restrict  )"Completed a computation phase: %d\n";
#line 45
    fprintf(__cil_tmp5, __cil_tmp6, i);
#line 46
    print_graph(graph.e_nodes, graph.h_nodes);
#line 41
    i = i + 1;
    }
  }
  while_11_break: /* CIL Label */ ;
  }
#line 48
  return (0);
}
}
