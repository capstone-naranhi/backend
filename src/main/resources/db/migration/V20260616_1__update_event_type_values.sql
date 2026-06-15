-- EventType 변경: PRONE_SUFFOCATION → UPSIDE_DOWN_SUFFOCATION, EXIT 제거
UPDATE safety_event SET event_type = 'UPSIDE_DOWN_SUFFOCATION' WHERE event_type = 'PRONE_SUFFOCATION';
UPDATE safety_notification SET event_type = 'UPSIDE_DOWN_SUFFOCATION' WHERE event_type = 'PRONE_SUFFOCATION';

DELETE FROM safety_notification WHERE event_type = 'EXIT';
DELETE FROM safety_event WHERE event_type = 'EXIT';
