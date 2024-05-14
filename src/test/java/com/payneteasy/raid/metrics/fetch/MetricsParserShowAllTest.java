package com.payneteasy.raid.metrics.fetch;


import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MetricsParserShowAllTest {

    private final MetricsParserShowAll parser = new MetricsParserShowAll();

    private static final String TEXT = """
                CLI Version = 007.2309.0000.0000 Sep 16, 2022
                Operating system = Linux 5.14.0-362.24.2.el9_3.x86_64
                Controller = 0
                Status = Success
                Description = None
                            
                            
                /c0/v0 :
                ======
                            
                ---------------------------------------------------------------
                DG/VD TYPE   State Access Consist Cache Cac sCC      Size Name\s
                ---------------------------------------------------------------
                0/0   RAID10 Optl  RW     No      RWTD  -   ON  20.957 TB data\s
                ---------------------------------------------------------------
                            
                VD=Virtual Drive| DG=Drive Group|Rec=Recovery
                Cac=CacheCade|OfLn=OffLine|Pdgd=Partially Degraded|Dgrd=Degraded
                Optl=Optimal|dflt=Default|RO=Read Only|RW=Read Write|HD=Hidden|TRANS=TransportReady
                B=Blocked|Consist=Consistent|R=Read Ahead Always|NR=No Read Ahead|WB=WriteBack
                AWB=Always WriteBack|WT=WriteThrough|C=Cached IO|D=Direct IO|sCC=Scheduled
                Check Consistency
                            
                            
                PDs for VD 0 :
                ============
                            
                --------------------------------------------------------------------------------------
                EID:Slt DID State DG     Size Intf Med SED PI SeSz Model                      Sp Type\s
                --------------------------------------------------------------------------------------
                252:1     2 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -   \s
                252:2     3 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -   \s
                252:3     4 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -   \s
                252:4     0 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -   \s
                252:5     7 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -   \s
                252:6     5 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -   \s
                --------------------------------------------------------------------------------------
                            
                EID=Enclosure Device ID|Slt=Slot No|DID=Device ID|DG=DriveGroup
                DHS=Dedicated Hot Spare|UGood=Unconfigured Good|GHS=Global Hotspare
                UBad=Unconfigured Bad|Sntze=Sanitize|Onln=Online|Offln=Offline|Intf=Interface
                Med=Media Type|SED=Self Encryptive Drive|PI=Protection Info
                SeSz=Sector Size|Sp=Spun|U=Up|D=Down|T=Transition|F=Foreign
                UGUnsp=UGood Unsupported|UGShld=UGood shielded|HSPShld=Hotspare shielded
                CFShld=Configured shielded|Cpybck=CopyBack|CBShld=Copyback Shielded
                UBUnsp=UBad Unsupported|Rbld=Rebuild
                            
                            
                VD0 Properties :
                ==============
                Strip Size = 256 KB
                Number of Blocks = 45005537280
                VD has Emulated PD = Yes
                Span Depth = 3
                Number of Drives Per Span = 2
                Write Cache(initial setting) = WriteThrough
                Disk Cache Policy = Disk's Default
                Encryption = None
                Data Protection = Disabled
                Active Operations = None
                Exposed to OS = Yes
                OS Drive Name = /dev/sda
                Creation Date = 13-05-2024
                Creation Time = 06:00:30 PM
                Emulation type = default
                Cachebypass size = Cachebypass-64k
                Cachebypass Mode = Cachebypass Intelligent
                Is LD Ready for OS Requests = Yes
                SCSI NAA Id = 600605b00b092a202dd511be155ef799
                Unmap Enabled = N/A
                        
                """;

    @Test
    public void extract_table_lines_dg_vd() {
        List<String> lines = parser.extractTableLines(TEXT, "DG/VD");
        lines.forEach(System.out::println);
        assertEquals(1, lines.size());
        assertEquals("0/0   RAID10 Optl  RW     No      RWTD  -   ON  20.957 TB data", lines.getFirst().trim());
    }

    @Test
    public void extract_table_lines_EID_Slt() {
        List<String> lines = parser.extractTableLines(TEXT, "EID:Slt");
        lines.forEach(System.out::println);
        assertEquals(6, lines.size());
        assertEquals("252:1     2 Onln   0 6.985 TB SATA SSD N   N  512B SAMSUNG MZ7L37T6HBLA-00A07 U  -", lines.getFirst().trim());
    }

    @Test
    public void parse_metrics() {
        List<Metric> metrics = parser.parseMetrics(TEXT);
    }


}