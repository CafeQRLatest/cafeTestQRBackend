-- Sync expense reference sequence with existing data
SELECT setval('expense_ref_seq', 
  COALESCE(
    (SELECT MAX(CAST(SUBSTRING(reference_number FROM 10) AS INTEGER)) 
     FROM expenses 
     WHERE reference_number LIKE 'EXP-%'),
    0
  ) + 1, 
  false
);
