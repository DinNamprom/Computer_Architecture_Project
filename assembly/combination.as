        lw      0       1       n       load n
        lw      0       2       r       load r
        lw      0       4       caddr   function address
        jalr    4       7               call combination
        halt                            answer is in register 3
comb    beq     2       0       base    C(n,0) = 1
        beq     1       2       base    C(n,n) = 1
        lw      0       6       one
        sw      5       7       stack   save return address
        add     5       6       5
        sw      5       1       stack   save n
        add     5       6       5
        sw      5       2       stack   save r
        add     5       6       5
        lw      0       6       neg1
        add     1       6       1       n = n - 1
        lw      0       4       caddr
        jalr    4       7               C(n-1,r)
        sw      5       3       stack   save first result
        lw      0       6       one
        add     5       6       5
        lw      0       6       neg1
        add     2       6       2       r = r - 1
        lw      0       4       caddr
        jalr    4       7               C(n-1,r-1)
        lw      0       6       neg1
        add     5       6       5
        lw      5       4       stack   recover first result
        add     4       3       3       sum both results
        add     5       6       5
        lw      5       2       stack   restore r
        add     5       6       5
        lw      5       1       stack   restore n
        add     5       6       5
        lw      5       7       stack   restore return address
        jalr    7       4               return
base    lw      0       3       one     base result
        jalr    7       4               return

