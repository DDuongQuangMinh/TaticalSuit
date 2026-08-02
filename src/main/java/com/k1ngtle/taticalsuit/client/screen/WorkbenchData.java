package com.k1ngtle.taticalsuit.client.screen;

public class WorkbenchData {

    public static final String[] SHORT_TAB_NAMES = {
        "AR", "BR", "LMG", "PDW", "SMG", "SHOTGUN", "SNIPER", "LAUNCHER"
    };

    public static final String[] IGNORE_KEYWORDS = {
            "_mag", "magazine", "drum", "ammo", "bullet", "nato", "parabellum", "buckshot", "acp", "cartridge",
            "scope", "sight", "optic", "reflex", "holo", "acog", "dot", "moa", "aimpoint", "srs", "rspec", 
            "specter", "hamr", "eaglescope", "spear", "hawk_scope", "wolf_scope", "drake_scope", "precision_scope", "delta", "hi_red", "operatorreflex",
            "grip", "underbarrel", "barrel", "muzzle", "suppressor", "silencer", "compensator", "brake", "choke", 
            "laser", "peq", "flashlight", "stock", "handguard", "bipod", "rail", "mount", "adapter",
            "m870modshotgun", "icon_", "ui_", "crafting_", "part", "receiver", "bolt", "spring", 
            "pin", "casing", "tube", "gas_block", "dust_cover", "sling", "charm", "sticker", 
            "camo", "paint", "spray", "skin", "blueprint", "pattern",
            "box", "case", "crate", "bundle", "key", "tool", "kit", 
            "manual", "guide", "shell", "projectile", "powder", "primer", "brass", 
            "steel", "polymer", "plastic", "wood", "cloth", "leather", "rubber", "glass", "lens", 
            "battery", "wire", "circuit", "chip", "board", "screen", "display", "sensor", "camera", 
            "button", "switch", "lever", "screw", "nut", "washer", "nail", "rivet",
            "helmet", "chestplate", "leggings", "boots", "vest", "armor", "plate", "nvg", "goggles", "mask",
            "gp25", "fn40", "m203launcher", "ulg99cannon", "m4sopmodii_bartender", "m4a1_asiimov", "l96a1_hyperbeast", "scarl_iss"
    };

    // --- WEAPON POOLS ---
    public static final String[] ASSAULT_RIFLE_IDS = new String[]{
            "pointblank:m4a1", "pointblank:m4a1mod1", "pointblank:m4sopmodii", 
            "pointblank:m16a1", "pointblank:hk416", "pointblank:scarl", 
            "pointblank:aug", "pointblank:g41", "pointblank:ak47", "pointblank:ak74", 
            "pointblank:ak12", "pointblank:an94", "pointblank:xm29"
    };

    public static final String[] BATTLE_RIFLE_IDS = new String[]{
            "pointblank:xm7", "pointblank:g36c", "pointblank:g36k"
    };

    public static final String[] LMG_IDS = new String[]{
            "pointblank:aughbar", "pointblank:lamg", "pointblank:mk48", "pointblank:m249", "pointblank:m134minigun"
    };

    public static final String[] PDW_IDS = new String[]{
            "pointblank:ar57", "pointblank:p90", "pointblank:m950", "pointblank:tmp", "pointblank:mp7"
    };

    public static final String[] SMG_IDS = new String[]{
            "pointblank:vector", "pointblank:mp5", "pointblank:ump45", "pointblank:mac10"
    };

    public static final String[] SHOTGUN_IDS = new String[]{
            "pointblank:m590", "pointblank:m870", "pointblank:spas12", "pointblank:m1014",
            "pointblank:aa12", "pointblank:citoricxs", "pointblank:hs12"
    };

    public static final String[] SNIPER_RIFLE_IDS = new String[]{
            "pointblank:sl8", "pointblank:mk14ebr", "pointblank:uar10", "pointblank:wa2000",
            "pointblank:xm3", "pointblank:c14", "pointblank:l96a1", "pointblank:ballista",
            "pointblank:gm6lynx", "pointblank:star15"
    };

    public static final String[] LAUNCHER_IDS = new String[]{
            "pointblank:smaw", "pointblank:at4", "pointblank:javelin", "pointblank:m32mgl"
    };

    public static final String[] SIDEARM_WEAPON_IDS = new String[]{
            "pointblank:glock17", "pointblank:glock18", "pointblank:m9", "pointblank:m1911a1",
            "pointblank:tti_viper", "pointblank:p30l", "pointblank:mk23", "pointblank:deserteagle",
            "pointblank:rhino"
    };

    // --- GEAR POOLS ---
    public static final String[] HELMET_IDS = new String[]{
            "NONE",
            "taticalsuit:helmet",
            "taticalsuit:helmet_pvs31",
            "taticalsuit:helmet_gpnvg18"
    };

    // --- ATTACHMENT POOLS ---
    public static final String[] OPTIC_IDS = {
            "NONE", 
            "pointblank:moa", 
            "pointblank:delta", 
            "pointblank:operatorreflex", 
            "pointblank:holographic",
            "pointblank:holographic_em",
            "pointblank:holographic558",
            "pointblank:aimpoint",
            "pointblank:aimpoint_t2",
            "pointblank:srs",
            "pointblank:rspec",
            "pointblank:acog",
            "pointblank:specter",
            "pointblank:hamr",
            "pointblank:hi_red",
            "pointblank:eaglescope",
            "pointblank:spear",
            "pointblank:spearblack",
            "pointblank:hawk_scope",
            "pointblank:wolf_scope",
            "pointblank:drake_scope",
            "pointblank:precision_scope",
    };
    
    public static final String[] BARREL_IDS = {"NONE", "pointblank:long_barrel", "pointblank:short_barrel"};
    
    public static final String[] MUZZLE_IDS = {
            "NONE", 
            "pointblank:ar_muzzlebrake", 
            "pointblank:smg_muzzlebrake", 
            "pointblank:p30l_compensator", 
            "pointblank:ar_suppressor", 
            "pointblank:ar_suppressor_tan", 
            "pointblank:xm7_suppressor", 
            "pointblank:ak_suppressor", 
            "pointblank:smg_suppressor", 
            "pointblank:rf_suppressor", 
            "pointblank:hp_suppressor", 
            "pointblank:sg_suppressor"
    };
    
    public static final String[] UNDERBARREL_IDS = {
            "NONE", 
            "pointblank:foregrip", 
            "pointblank:foregrip_tan", 
            "pointblank:shortgrip", 
            "pointblank:stubbygrip", 
            "pointblank:stubbygriptan", 
            "pointblank:heragrip", 
            "pointblank:ak_romgrip", 
            "pointblank:m203launcher", 
            "pointblank:gp25", 
            "pointblank:m870modshotgun", 
            "pointblank:ulg99cannon", 
            "pointblank:fn40"
    };
    
    public static final String[] LASER_IDS = {"NONE", "pointblank:peq15", "pointblank:tlr7", "pointblank:flashlight"};

    public static final String[] SIDEARM_OPTIC_IDS = {
            "NONE", 
            "pointblank:moa_hg"
    };

    public static final String[] SIDEARM_MUZZLE_IDS = {
            "NONE", 
            "pointblank:p30l_compensator", 
            "pointblank:smg_suppressor",
            "pointblank:smg_muzzlebrake",
    };

    public static final String[] SIDEARM_STOCK_IDS = {
            "NONE", 
            "pointblank:m9_stock", 
            "pointblank:glock_stock"
    };
}