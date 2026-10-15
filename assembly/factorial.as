floop   lw      0       3       mplier         # $3 = mplier = n
        lw      0       1       pos1
        beq     3       0       done            # n == 0 -> finished (0! = 1)
        beq     3       1       done            # n == 1 -> finished
        lw      0       2       res     $2 = res = mcand (shift left ทุกรอบ) --Multiply starts here
        lw      0       4       pos1    $4 = mask = 1
        lw      0       6       lim     $6 = 32768 (ค่าที่ใช้เช็คว่า mask ครบ 15 bits)
loop    nand    3       4       5       $5 = NAND(mplier, mask)
        nand    5       5       5       $5 = AND(mplier, mask)
        beq     5       0       1       ถ้า AND == 0 (bit เป็น 0) ข้าม add
        add     1       2       1       product ($1) += mcand
        add     2       2       2       mcand = mcand * 2 (shift left)
        add     4       4       4       mask = mask * 2 (shift left)
        beq     4       6       1       ถ้า mask == 32768 กระโดดไป halt
        beq     0       0       loop    ยังไม่ครบ วนกลับ --Multiply done, store result and decrement mplier
        sw      0       1       res     # store result
        lw      0       3       mplier  # load mplier
        lw      0       4       neg1    # load -1
        add     3       4       3       # mplier = mplier - 1
        sw      0       3       mplier  # store mplier
        beq     0       0       floop   # loop back to floop
done    lw      0       1       res     # load result
        halt
res     .fill   1   #mcand
mplier  .fill   5
pos1    .fill   1
neg1    .fill   -1
lim     .fill   32768
