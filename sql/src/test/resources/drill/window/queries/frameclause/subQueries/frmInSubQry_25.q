SELECT *
    FROM
    ( SELECT MIN(c5) OVER W as w_min,
             MIN(c5) OVER W2 as w2_min,
             MIN(c5) OVER W3 as w3_min
      FROM "t_alltype.parquet"
          WINDOW W AS ( PARTITION BY c8 ORDER BY c1 RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING ),
          W2 AS ( PARTITION BY c8 ORDER BY c1 ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW ),
          W3 AS ( PARTITION BY c8 ORDER BY c1 RANGE BETWEEN CURRENT ROW AND CURRENT ROW )
    ) subQry
