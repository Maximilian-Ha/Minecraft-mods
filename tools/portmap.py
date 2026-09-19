"""Die Abbildungen zwischen Original und Port, die keine Regel faengt.

Zwei Werkzeuge brauchen sie -- tools/port-gap.py und tools/be-blocker.py --, und sie duerfen
nicht auseinanderlaufen. Darum stehen sie hier einmal.

JEDE ZEILE IST NACHGESEHEN, nicht geraten. Wer eine hinzufuegt, schreibt dazu, woran er es
festgemacht hat: ein Klassenkommentar im Port, der die Vorlage nennt, oder gelesener Code mit
gleicher Wirkung. Blosse Namensaehnlichkeit reicht NICHT -- TileEntityCore ist der Bombenkern
und nicht PileCoreBlockEntity, TileEntityCharge die Sprengladung und nicht das Ladegeraet.
"""

# Blockentitaeten, die im Port anders heissen.
UMBENANNT = {
    # Das "NT" des Originals faellt im Port weg.
    'TileEntityCableBaseNT':       'CableBaseBlockEntity',
    'TileEntityPipeBaseNT':        'PipeBaseBlockEntity',
    'TileEntityTurretBaseNT':      'TurretBaseBlockEntity',
    # Abgekuerzter Name im Original, ausgeschriebener im Port -- gleiche Wirkung: Strahlung
    # senken, Kontaminationen loeschen, Partikel darueber.
    'TileEntityDecon':             'DecontaminatorBlockEntity',
    # Der Port fasst die Handsteuerung mit der allgemeinen Steuerstange zusammen; der
    # Klassenkommentar von RBMKControlBlockEntity nennt beide Vorlagen.
    'TileEntityRBMKControlManual': 'RBMKControlBlockEntity',
    # Die Haehne heissen im Port nach dem Rohr, an dem sie sitzen, nicht nach dem Fluid.
    'TileEntityFluidValve':        'PipeValveBlockEntity',
    'TileEntityFluidCounterValve': 'PipeCounterValveBlockEntity',
    # Die drei Stellvertreter des Originals sind im Port einer: ProxyComboBlockEntity
    # implementiert Energie, Leiter, Inventar und Fluid zugleich.
    'TileEntityProxyConductor':    'ProxyComboBlockEntity',
    'TileEntityProxyEnergy':       'ProxyComboBlockEntity',
    'TileEntityProxyInventory':    'ProxyComboBlockEntity',
}

# Infrastruktur des Originals, die im Port anders heisst oder durch Vanilla ersetzt ist.
# Der Wert ist eine Notiz; gewertet wird allein, DASS der Name hier steht.
INFRASTRUKTUR = {
    'ModItems':            'NtmItems',
    'ModBlocks':           'NtmBlocks',
    'ModDamageSource':     'NtmDamageTypes',
    'MainRegistry':        'NuclearTechMod',
    'NTMSounds':           'NtmSoundEvents',
    'IGUIProvider':        'MenuProvider bzw. IScreenProvider',
    'BlockDummyable':      'DummyableBlock',
    'BlockPos':            'net.minecraft.core.BlockPos -- im Original com.hbm.util.fauxpointtwelve',
    'Vec3':                'net.minecraft.world.phys.Vec3',
    'PacketDispatcher':    'PacketDistributor von NeoForge',
    'PacketThreading':     'PacketDistributor von NeoForge',
    'AuxParticlePacketNT': 'ParticleCreators',
    'BufferUtil':          'RegistryFriendlyByteBuf',
    'CompatHandler':       'nicht portiert -- OpenComputers ist im Port nicht angebunden',
    'CompatEnergyControl': 'nicht portiert -- Energy Control ist im Port nicht angebunden',
    'Compat':              'im Port ueber getBlockEntity direkt',
}
