ALTER TABLE flight_payslip
	ALTER COLUMN overtime_hours TYPE INTEGER;
ALTER TABLE flight_payslip
	RENAME COLUMN overtime_hours TO overtime_duration;

ALTER TABLE flight_duty
	DROP COLUMN current_inbound_duration;
ALTER TABLE flight_duty
	DROP COLUMN current_outbound_duration;
