/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 18 "simple_dma.h"
struct _control_block {
   unsigned int chunk_size ;
   unsigned char pad[4] ;
   unsigned long long addr ;
};
#line 18 "simple_dma.h"
typedef struct _control_block control_block;
#line 203 "../spu_mfcio.h"
extern void mfc_put(void volatile   *ls , unsigned int ea , unsigned int size , unsigned int tag ,
                    unsigned int tid , unsigned int rid ) ;
#line 211
extern void mfc_get(void volatile   *ls , unsigned int ea , unsigned int size , unsigned int tag ,
                    unsigned int tid , unsigned int rid ) ;
#line 252
extern void mfc_write_tag_mask(unsigned int mask ) ;
#line 270
extern void mfc_read_tag_status_all() ;
#line 361
extern unsigned int __mfc_tag_reserve(void) ;
#line 339 "/usr/include/stdio.h"
extern int printf(char const   * __restrict  __format  , ...) ;
#line 15 "simple_dma_spu.c"
control_block volatile   cb  __attribute__((__aligned__(128)))  ;
#line 18 "simple_dma_spu.c"
int data[64]  __attribute__((__aligned__(128)))  ;
#line 44
extern int __VERIFIER_nondet_int() ;
#line 21 "simple_dma_spu.c"
int spu_main(unsigned long long speid  __attribute__((__unused__)) , unsigned long long argp ,
             unsigned long long envp  __attribute__((__unused__)) ) 
{ int i ;
  int error ;
  unsigned int tag_id ;
  int tmp ;
  int tmp___0 ;
  int tmp___1 ;
  int tmp___2 ;
  int tmp___3 ;
  int tmp___4 ;
  char const   * __restrict  __cil_tmp13 ;
  void volatile   *__cil_tmp14 ;
  unsigned int __cil_tmp15 ;
  unsigned int __cil_tmp16 ;
  control_block volatile   *__cil_tmp17 ;
  unsigned long __cil_tmp18 ;
  unsigned long __cil_tmp19 ;
  unsigned long __cil_tmp20 ;
  unsigned long __cil_tmp21 ;
  unsigned long __cil_tmp22 ;
  unsigned long __cil_tmp23 ;
  unsigned long __cil_tmp24 ;
  unsigned long __cil_tmp25 ;
  unsigned long __cil_tmp26 ;
  unsigned long __cil_tmp27 ;
  unsigned long __cil_tmp28 ;
  unsigned long __cil_tmp29 ;
  unsigned long __cil_tmp30 ;
  int __cil_tmp31 ;
  unsigned int __cil_tmp32 ;
  unsigned long __cil_tmp33 ;
  unsigned long __cil_tmp34 ;
  int *__cil_tmp35 ;
  void volatile   *__cil_tmp36 ;
  unsigned long __cil_tmp37 ;
  unsigned long long volatile   __cil_tmp38 ;
  unsigned int __cil_tmp39 ;
  unsigned long __cil_tmp40 ;
  unsigned int __cil_tmp41 ;
  unsigned long __cil_tmp42 ;
  unsigned long __cil_tmp43 ;
  int *__cil_tmp44 ;
  void volatile   *__cil_tmp45 ;
  unsigned long __cil_tmp46 ;
  unsigned long long volatile   __cil_tmp47 ;
  unsigned int __cil_tmp48 ;
  unsigned long __cil_tmp49 ;
  unsigned int __cil_tmp50 ;
  char const   * __restrict  __cil_tmp51 ;
  unsigned long __cil_tmp52 ;
  unsigned long long volatile   __cil_tmp53 ;
  int __cil_tmp54 ;
  unsigned long __cil_tmp55 ;
  unsigned long __cil_tmp56 ;
  int __cil_tmp57 ;
  int __cil_tmp58 ;
  unsigned long __cil_tmp59 ;
  unsigned long __cil_tmp60 ;
  int __cil_tmp61 ;
  int __cil_tmp62 ;
  unsigned long __cil_tmp63 ;
  unsigned long __cil_tmp64 ;
  int __cil_tmp65 ;
  char const   * __restrict  __cil_tmp66 ;
  int __cil_tmp67 ;
  unsigned long __cil_tmp68 ;
  unsigned long __cil_tmp69 ;
  int __cil_tmp70 ;
  int __cil_tmp71 ;
  unsigned long __cil_tmp72 ;
  unsigned long __cil_tmp73 ;
  int __cil_tmp74 ;
  int __cil_tmp75 ;
  unsigned long __cil_tmp76 ;
  unsigned long __cil_tmp77 ;
  int __cil_tmp78 ;

  {
  {
#line 29
  tag_id = __mfc_tag_reserve();
  }
#line 29
  if (tag_id == 4294967295U) {
    {
#line 30
    __cil_tmp13 = (char const   * __restrict  )"ERROR: unable to reserve a tag\n";
#line 30
    printf(__cil_tmp13);
    }
#line 31
    return (1);
  } else {

  }
  {
#line 43
  __cil_tmp14 = (void volatile   *)(& cb);
#line 43
  __cil_tmp15 = (unsigned int )argp;
#line 43
  __cil_tmp16 = (unsigned int )16UL;
#line 43
  mfc_get(__cil_tmp14, __cil_tmp15, __cil_tmp16, tag_id, 0U, 0U);
#line 44
  tmp = __VERIFIER_nondet_int();
#line 44
  __cil_tmp17 = & cb;
#line 44
  *((unsigned int volatile   *)__cil_tmp17) = (unsigned int volatile   )tmp;
#line 44
  tmp___0 = __VERIFIER_nondet_int();
#line 44
  __cil_tmp18 = 0 * 1UL;
#line 44
  __cil_tmp19 = 4 + __cil_tmp18;
#line 44
  __cil_tmp20 = (unsigned long )(& cb) + __cil_tmp19;
#line 44
  *((unsigned char volatile   *)__cil_tmp20) = (unsigned char )tmp___0;
#line 44
  tmp___1 = __VERIFIER_nondet_int();
#line 44
  __cil_tmp21 = 1 * 1UL;
#line 44
  __cil_tmp22 = 4 + __cil_tmp21;
#line 44
  __cil_tmp23 = (unsigned long )(& cb) + __cil_tmp22;
#line 44
  *((unsigned char volatile   *)__cil_tmp23) = (unsigned char )tmp___1;
#line 44
  tmp___2 = __VERIFIER_nondet_int();
#line 44
  __cil_tmp24 = 2 * 1UL;
#line 44
  __cil_tmp25 = 4 + __cil_tmp24;
#line 44
  __cil_tmp26 = (unsigned long )(& cb) + __cil_tmp25;
#line 44
  *((unsigned char volatile   *)__cil_tmp26) = (unsigned char )tmp___2;
#line 44
  tmp___3 = __VERIFIER_nondet_int();
#line 44
  __cil_tmp27 = 3 * 1UL;
#line 44
  __cil_tmp28 = 4 + __cil_tmp27;
#line 44
  __cil_tmp29 = (unsigned long )(& cb) + __cil_tmp28;
#line 44
  *((unsigned char volatile   *)__cil_tmp29) = (unsigned char )tmp___3;
#line 44
  tmp___4 = __VERIFIER_nondet_int();
#line 44
  __cil_tmp30 = (unsigned long )(& cb) + 8;
#line 44
  *((unsigned long long volatile   *)__cil_tmp30) = (unsigned long long volatile   )tmp___4;
#line 49
  __cil_tmp31 = 1 << tag_id;
#line 49
  __cil_tmp32 = (unsigned int )__cil_tmp31;
#line 49
  mfc_write_tag_mask(__cil_tmp32);
#line 53
  mfc_read_tag_status_all();
#line 56
  __cil_tmp33 = 0 * 4UL;
#line 56
  __cil_tmp34 = (unsigned long )(data) + __cil_tmp33;
#line 56
  __cil_tmp35 = (int *)__cil_tmp34;
#line 56
  __cil_tmp36 = (void volatile   *)__cil_tmp35;
#line 56
  __cil_tmp37 = (unsigned long )(& cb) + 8;
#line 56
  __cil_tmp38 = *((unsigned long long volatile   *)__cil_tmp37);
#line 56
  __cil_tmp39 = (unsigned int )__cil_tmp38;
#line 56
  __cil_tmp40 = 64UL * 4UL;
#line 56
  __cil_tmp41 = (unsigned int )__cil_tmp40;
#line 56
  mfc_get(__cil_tmp36, __cil_tmp39, __cil_tmp41, tag_id, 0U, 0U);
#line 60
  __cil_tmp42 = 0 * 4UL;
#line 60
  __cil_tmp43 = (unsigned long )(data) + __cil_tmp42;
#line 60
  __cil_tmp44 = (int *)__cil_tmp43;
#line 60
  __cil_tmp45 = (void volatile   *)__cil_tmp44;
#line 60
  __cil_tmp46 = (unsigned long )(& cb) + 8;
#line 60
  __cil_tmp47 = *((unsigned long long volatile   *)__cil_tmp46);
#line 60
  __cil_tmp48 = (unsigned int )__cil_tmp47;
#line 60
  __cil_tmp49 = 64UL * 4UL;
#line 60
  __cil_tmp50 = (unsigned int )__cil_tmp49;
#line 60
  mfc_put(__cil_tmp45, __cil_tmp48, __cil_tmp50, tag_id, 0U, 0U);
#line 63
  __cil_tmp51 = (char const   * __restrict  )"Address received through control block = 0x%llx\n";
#line 63
  __cil_tmp52 = (unsigned long )(& cb) + 8;
#line 63
  __cil_tmp53 = *((unsigned long long volatile   *)__cil_tmp52);
#line 63
  printf(__cil_tmp51, __cil_tmp53);
#line 67
  mfc_read_tag_status_all();
#line 69
  error = 0;
#line 73
  i = 2;
  }
  {
#line 73
  while (1) {
    while_continue: /* CIL Label */ ;
#line 73
    if (! error) {
#line 73
      if (i < 64) {

      } else {
#line 73
        goto while_break;
      }
    } else {
#line 73
      goto while_break;
    }
    {
#line 74
    __cil_tmp54 = i - 2;
#line 74
    __cil_tmp55 = __cil_tmp54 * 4UL;
#line 74
    __cil_tmp56 = (unsigned long )(data) + __cil_tmp55;
#line 74
    __cil_tmp57 = *((int *)__cil_tmp56);
#line 74
    __cil_tmp58 = i - 1;
#line 74
    __cil_tmp59 = __cil_tmp58 * 4UL;
#line 74
    __cil_tmp60 = (unsigned long )(data) + __cil_tmp59;
#line 74
    __cil_tmp61 = *((int *)__cil_tmp60);
#line 74
    __cil_tmp62 = __cil_tmp61 + __cil_tmp57;
#line 74
    __cil_tmp63 = i * 4UL;
#line 74
    __cil_tmp64 = (unsigned long )(data) + __cil_tmp63;
#line 74
    __cil_tmp65 = *((int *)__cil_tmp64);
#line 74
    if (__cil_tmp65 != __cil_tmp62) {
      {
#line 75
      __cil_tmp66 = (char const   * __restrict  )"ERROR: fibonacci sequence error at entry %d. Expected %d, Got %d\n";
#line 75
      __cil_tmp67 = i - 2;
#line 75
      __cil_tmp68 = __cil_tmp67 * 4UL;
#line 75
      __cil_tmp69 = (unsigned long )(data) + __cil_tmp68;
#line 75
      __cil_tmp70 = *((int *)__cil_tmp69);
#line 75
      __cil_tmp71 = i - 1;
#line 75
      __cil_tmp72 = __cil_tmp71 * 4UL;
#line 75
      __cil_tmp73 = (unsigned long )(data) + __cil_tmp72;
#line 75
      __cil_tmp74 = *((int *)__cil_tmp73);
#line 75
      __cil_tmp75 = __cil_tmp74 + __cil_tmp70;
#line 75
      __cil_tmp76 = i * 4UL;
#line 75
      __cil_tmp77 = (unsigned long )(data) + __cil_tmp76;
#line 75
      __cil_tmp78 = *((int *)__cil_tmp77);
#line 75
      printf(__cil_tmp66, i, __cil_tmp75, __cil_tmp78);
#line 77
      error = 1;
      }
    } else {

    }
    }
#line 73
    i = i + 1;
  }
  while_break: /* CIL Label */ ;
  }
#line 81
  return (error);
}
}
