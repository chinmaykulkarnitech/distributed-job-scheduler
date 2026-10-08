-- Step 2: scope workers to an organization.
-- Run once against an existing database (fresh installs get this from schema.sql).
ALTER TABLE workers
  ADD COLUMN organization_id CHAR(36) NULL,
  ADD KEY idx_workers_org (organization_id),
  ADD CONSTRAINT fk_workers_org FOREIGN KEY (organization_id) REFERENCES organizations(id);

-- Workers registered before this change have no organization and can no longer claim jobs.
-- Re-register them (or assign an org manually):
-- UPDATE workers SET organization_id = '<org-id>' WHERE organization_id IS NULL;
