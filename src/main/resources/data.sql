INSERT INTO empleados (nombre, email, rol, password, estado, fecha_registro)
SELECT 'Administrador', 'admin@empresa.com', 'admin', '$2a$10$mzFjljU7fHIl.YB9/pbVTussLROV/N.YZCVsa4rcYf9As2rLcIyfm', 'ACTIVO', now()
WHERE NOT EXISTS (
    SELECT 1 FROM empleados WHERE email = 'admin@empresa.com'
);
