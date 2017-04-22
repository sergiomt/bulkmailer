ALTER TABLE k_microsites DROP CONSTRAINT f1_microsites;
ALTER TABLE k_microsites DROP CONSTRAINT f2_microsites;

ALTER TABLE k_pagesets DROP CONSTRAINT f1_pagesets;
ALTER TABLE k_pagesets DROP CONSTRAINT f2_pagesets;
ALTER TABLE k_pagesets DROP CONSTRAINT f3_pagesets;
ALTER TABLE k_pagesets DROP CONSTRAINT f4_pagesets;
ALTER TABLE k_pagesets DROP CONSTRAINT f5_pagesets;

ALTER TABLE k_pagesets_lookup DROP CONSTRAINT f1_pagesets_lookup;

ALTER TABLE k_pageset_pages DROP CONSTRAINT f1_pageset_pages;

DROP TABLE k_x_pageset_list;
DROP TABLE k_pageset_pages;
DROP TABLE k_pagesets_lookup;
DROP TABLE k_pagesets;
DROP TABLE k_microsites;